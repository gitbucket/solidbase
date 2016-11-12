package io.github.gitbucket.solidbase;

import io.github.gitbucket.solidbase.migration.AntMigration;
import io.github.gitbucket.solidbase.migration.LiquibaseMigration;
import io.github.gitbucket.solidbase.model.Module;
import io.github.gitbucket.solidbase.model.Version;
import liquibase.database.core.H2Database;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import static org.junit.Assert.*;
import static io.github.gitbucket.solidbase.migration.MigrationUtils.*;

public class SolidbaseTest {

    @Test
    public void test() throws Exception {
        Module module = new Module(
            "test",
            new Version("1.0.0", new LiquibaseMigration(), new AntMigration("test-ant_1.0.0.xml"))
        );

        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "sa");
        Path path = Paths.get("solidbase-test-dir");

        try {
            Solidbase solidbase = new Solidbase();
            solidbase.migrate(conn, Thread.currentThread().getContextClassLoader(), new H2Database(), module);

            Integer count = selectIntFromDatabase(conn, "SELECT COUNT(*) FROM PERSON");
            assertEquals(0, count.intValue());

            String version = selectStringFromDatabase(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID='test'");
            assertEquals("1.0.0", version);

            assertTrue(Files.exists(path));
            assertTrue(Files.isDirectory(path));

        } finally {
            ignoreException(() -> conn.close());
            ignoreException(() -> Files.delete(path));
        }
    }

}
