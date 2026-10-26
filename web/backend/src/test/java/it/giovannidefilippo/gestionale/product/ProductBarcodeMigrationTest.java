package it.giovannidefilippo.gestionale.product;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductBarcodeMigrationTest {
    @Test
    void existingProductsBecomePiecesWithoutBarcodeAndDatabaseGuardsUniqueness() {
        String database = "product_barcode_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        Flyway.configure().dataSource(dataSource).target("36").load().migrate();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        long hardware = jdbc.queryForObject("select id from product_categories where code_canonical = 'hardware'", Long.class);
        jdbc.update("insert into products (id, version, code, code_canonical, name, description, category_id, brand, product_type, usage_context, quantity, reserved_quantity, price, discount, discontinued, costed_quantity) values (7101, 0, 'OLD-1', 'old-1', 'Storico', 'Prodotto storico.', ?, 'Brand', 'Tipo', '', 4, 0, 10, 0, false, 0)", hardware);

        Flyway upgrade = Flyway.configure().dataSource(dataSource).target("37").load();
        upgrade.migrate();

        assertThat(upgrade.info().current().getVersion().getVersion()).isEqualTo("37");
        assertThat(jdbc.queryForObject("select unit_of_measure from products where id = 7101", String.class)).isEqualTo("PIECE");
        assertThat(jdbc.queryForObject("select barcode from products where id = 7101", String.class)).isNull();

        jdbc.update("update products set barcode = 'Sku-1', barcode_canonical = 'sku-1' where id = 7101");
        jdbc.update("insert into products (id, version, code, code_canonical, name, description, category_id, brand, product_type, usage_context, quantity, reserved_quantity, price, discount, discontinued, costed_quantity, unit_of_measure) values (7102, 0, 'NEW-1', 'new-1', 'Nuovo', 'Prodotto nuovo.', ?, 'Brand', 'Tipo', '', 0, 0, 10, 0, false, 0, 'PAIR')", hardware);
        assertThatThrownBy(() -> jdbc.update("update products set barcode = 'SKU-1', barcode_canonical = 'sku-1' where id = 7102"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update products set barcode = 'SKU-2', barcode_canonical = 'other' where id = 7102"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update products set unit_of_measure = 'BOX' where id = 7102"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
