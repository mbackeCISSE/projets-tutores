package backend.backend;

import java.util.UUID;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Ces tests utilisent une nouvelle base dediee pour ne pas modifier la base de demonstration. */
@EnabledIfSystemProperty(named = "tests.mysql", matches = "true")
class MySqlBackendTests extends BackendApplicationTests {
    private static final String BASE = "projets_tutores_test_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void configurerMySql(DynamicPropertyRegistry registre) {
        registre.add("spring.datasource.url", () -> "jdbc:mysql://localhost:3306/" + BASE
                + "?createDatabaseIfNotExist=true&serverTimezone=UTC");
        registre.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registre.add("spring.datasource.username", () -> System.getenv().getOrDefault("DB_USERNAME", "root"));
        registre.add("spring.datasource.password", () -> System.getenv().getOrDefault("DB_PASSWORD", ""));
        registre.add("spring.datasource.hikari.connection-init-sql", () -> "SET time_zone = '+00:00'");
        System.out.println("Base MySQL de verification : " + BASE);
    }
}
