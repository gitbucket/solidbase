package io.github.gitbucket.solidbase;

import io.github.gitbucket.solidbase.migration.LiquibaseMigration;
import io.github.gitbucket.solidbase.migration.MigrationUtils;
import io.github.gitbucket.solidbase.model.Module;
import io.github.gitbucket.solidbase.model.Version;
import liquibase.database.core.H2Database;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import static org.junit.Assert.*;

public class SolidbaseTest {

    @Test
    public void test() throws Exception {
        Module module = new Module(
            "test",
            new Version("1.0.0", new LiquibaseMigration())
        );

        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "sa");

        Solidbase solidbase = new Solidbase();
        solidbase.migrate(conn, Thread.currentThread().getContextClassLoader(), new H2Database(), module);

        Integer count = MigrationUtils.selectInt(conn, "SELECT COUNT(*) FROM PERSON");
        assertEquals(0, count.intValue());

        String version = MigrationUtils.selectString(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID='test'");
        assertEquals("1.0.0", version);

        conn.close();
    }

}
