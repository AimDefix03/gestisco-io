package it.giovannidefilippo.gestionale.product;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnitOfMeasureMigrationTest {
    @Test
    void historicalUnitsBecomeConfigurableRowsAndProductsKeepTheirUnit() {
        String database = "units_of_measure_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        Flyway.configure().dataSource(dataSource).target("39").load().migrate();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        long hardware = jdbc.queryForObject("select id from product_categories where code_canonical = 'hardware'", Long.class);
        long vatRate = jdbc.queryForObject("select id from vat_rates where default_rate = true", Long.class);
        jdbc.update("insert into products (id, version, code, code_canonical, name, description, category_id, brand, product_type, usage_context, quantity, reserved_quantity, price, discount, discontinued, costed_quantity, unit_of_measure, vat_rate_id) values (7201, 0, 'SHOE-1', 'shoe-1', 'Scarpa', 'Scarpa storica.', ?, 'Brand', 'Calzatura', '', 4, 0, 90, 0, false, 0, 'PAIR', ?)", hardware, vatRate);

        Flyway upgrade = Flyway.configure().dataSource(dataSource).target("40").load();
        upgrade.migrate();

        assertThat(upgrade.info().current().getVersion().getVersion()).isEqualTo("40");
        assertThat(jdbc.queryForList("select code from units_of_measure where active order by code", String.class))
                .containsExactly("KILOGRAM", "LITER", "METER", "PACK", "PAIR", "PIECE");
        assertThat(jdbc.queryForObject("select label from units_of_measure u join products p on p.unit_of_measure = u.code where p.id = 7201", String.class))
                .isEqualTo("Paio");

        jdbc.update("insert into units_of_measure (code, label, symbol, created_at, updated_at) values ('ROLL', 'Rotolo', 'rot', current_timestamp, current_timestamp)");
        jdbc.update("update products set unit_of_measure = 'ROLL' where id = 7201");
        assertThatThrownBy(() -> jdbc.update("update products set unit_of_measure = 'BOX' where id = 7201"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("insert into units_of_measure (code, label, symbol, created_at, updated_at) values ('roll2', 'Minuscolo', 'x', current_timestamp, current_timestamp)"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update units_of_measure set active = false where code = 'PIECE'"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
