package it.giovannidefilippo.gestionale.product;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductCategoryMigrationTest {
    @Test
    void movesEveryHistoricalProductToAConfigurableCategory() {
        String database = "product_categories_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        Flyway.configure().dataSource(dataSource).target("35").load().migrate();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        insertProduct(jdbc, 6101, "HW-1", "HARDWARE", 7);
        insertProduct(jdbc, 6102, "SW-1", "SOFTWARE", 3);
        insertProduct(jdbc, 6103, "LEG-1", "Accessori", 2);
        insertProduct(jdbc, 6104, "LEG-2", " accessori ", 1);

        Flyway upgrade = Flyway.configure().dataSource(dataSource).target("36").load();
        upgrade.migrate();

        assertThat(upgrade.info().current().getVersion().getVersion()).isEqualTo("36");
        assertThat(jdbc.queryForObject("select count(*) from product_categories", Long.class)).isEqualTo(3);
        assertThat(categoryOf(jdbc, 6101)).isEqualTo("HARDWARE");
        assertThat(categoryOf(jdbc, 6102)).isEqualTo("SOFTWARE");
        assertThat(categoryOf(jdbc, 6103)).isEqualTo("Accessori");
        assertThat(categoryOf(jdbc, 6104)).isEqualTo("Accessori");
        assertThat(jdbc.queryForObject("select sum(quantity) from products", Long.class)).isEqualTo(13);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns where table_name = 'products' and column_name = 'category'", Long.class)).isZero();

        long hardwareId = jdbc.queryForObject("select id from product_categories where code_canonical = 'hardware'", Long.class);
        assertThatThrownBy(() -> jdbc.update("insert into product_categories (code, code_canonical, name, name_canonical, parent_key, active, created_at, updated_at) values ('HW2', 'hw2', 'HARDWARE', 'hardware', 0, true, current_timestamp, current_timestamp)"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("insert into product_categories (code, code_canonical, name, name_canonical, parent_id, parent_key, active, created_at, updated_at) values ('GPU', 'gpu', 'Schede', 'schede', ?, 0, true, current_timestamp, current_timestamp)", hardwareId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update products set category_id = null where id = 6101"))
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbc.update("insert into product_categories (code, code_canonical, name, name_canonical, parent_id, parent_key, active, created_at, updated_at) values ('GPU', 'gpu', 'Hardware', 'hardware', ?, ?, true, current_timestamp, current_timestamp)", hardwareId, hardwareId);
    }

    private static void insertProduct(JdbcTemplate jdbc, long id, String code, String category, int quantity) {
        jdbc.update("insert into products (id, version, code, code_canonical, name, description, category, brand, product_type, usage_context, quantity, reserved_quantity, price, discount, discontinued, costed_quantity) values (?, 0, ?, ?, 'Prodotto storico', 'Fixture migrazione categorie.', ?, 'Brand', 'Tipo', '', ?, 0, 10, 0, false, 0)",
                id, code, code.toLowerCase(), category, quantity);
    }

    private static String categoryOf(JdbcTemplate jdbc, long productId) {
        return jdbc.queryForObject("select c.code from products p join product_categories c on c.id = p.category_id where p.id = ?", String.class, productId);
    }
}
