package io.github.gitbucket.solidbase;

import io.github.gitbucket.solidbase.migration.AntMigration;
import io.github.gitbucket.solidbase.migration.LiquibaseMigration;
import io.github.gitbucket.solidbase.model.Module;
import io.github.gitbucket.solidbase.model.Version;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import static org.junit.Assert.*;
import static io.github.gitbucket.solidbase.migration.MigrationUtils.*;

public class SolidbaseTest {

    @Test
    public void testWithH2() throws Exception {
        Module module = new Module(
            "test",
            new Version("1.0.0", new LiquibaseMigration(), new AntMigration("test-ant_1.0.0.xml"))
        );

        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "sa")) {
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
            }
            finally {
                ignoreException(() -> Files.delete(path));
            }
        }
    }

    @Test
    public void testWithSQLite() throws Exception {
        Module module = new Module(
                "test",
                new Version("1.0.0", new LiquibaseMigration(), new AntMigration("test-ant_1.0.0.xml"))
        );

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:", "sa", "sa")) {
            Path path = Paths.get("solidbase-test-dir");

            try {
                Solidbase solidbase = new Solidbase();
                solidbase.migrate(conn, Thread.currentThread().getContextClassLoader(), new SQLiteDatabase(), module);

                Integer count = selectIntFromDatabase(conn, "SELECT COUNT(*) FROM PERSON");
                assertEquals(0, count.intValue());

                String version = selectStringFromDatabase(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID='test'");
                assertEquals("1.0.0", version);

                assertTrue(Files.exists(path));
                assertTrue(Files.isDirectory(path));
            }
            finally {
                ignoreException(() -> conn.close());
                ignoreException(() -> Files.delete(path));
            }
        }
    }

    @Test
    public void testWithMySQL() throws Exception {
        Module module = new Module(
                "test",
                new Version("1.0.0", new LiquibaseMigration(), new AntMigration("test-ant_1.0.0.xml"))
        );

        try (MySQLContainer container = new MySQLContainer("mysql:8")) {
            container.start();

            try (Connection conn = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
                Path path = Paths.get("solidbase-test-dir");

                try {
                    Solidbase solidbase = new Solidbase();
                    solidbase.migrate(conn, Thread.currentThread().getContextClassLoader(), new MySQLDatabase(), module);

                    Integer count = selectIntFromDatabase(conn, "SELECT COUNT(*) FROM person");
                    assertEquals(0, count.intValue());

                    String version = selectStringFromDatabase(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID='test'");
                    assertEquals("1.0.0", version);

                    assertTrue(Files.exists(path));
                    assertTrue(Files.isDirectory(path));
                }
                finally {
                    ignoreException(() -> conn.close());
                    ignoreException(() -> Files.delete(path));
                }
            }
        }
    }

    @Test
    public void testWithPostgreSQL() throws Exception {
        Module module = new Module(
                "test",
                new Version("1.0.0", new LiquibaseMigration(), new AntMigration("test-ant_1.0.0.xml"))
        );

        try (PostgreSQLContainer container = new PostgreSQLContainer("postgres:11")) {
            container.start();

            try (Connection conn = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
                Path path = Paths.get("solidbase-test-dir");

                try {
                    Solidbase solidbase = new Solidbase();
                    solidbase.migrate(conn, Thread.currentThread().getContextClassLoader(), new PostgresDatabase(), module);

                    Integer count = selectIntFromDatabase(conn, "SELECT COUNT(*) FROM PERSON");
                    assertEquals(0, count.intValue());

                    String version = selectStringFromDatabase(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID='test'");
                    assertEquals("1.0.0", version);

                    assertTrue(Files.exists(path));
                    assertTrue(Files.isDirectory(path));
                }
                finally {
                    ignoreException(() -> conn.close());
                    ignoreException(() -> Files.delete(path));
                }
            }
        }
    }
}
