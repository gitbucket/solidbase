package io.github.gitbucket.solidbase.manager;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.junit.Assert.*;

public class JDBCVersionManagerTest {

    @Test(expected = Exception.class)
    public void testGetCurrentVersionThrowsWhenVersionsTableIsMissing() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test-getCurrentVersion-missing-table", "sa", "sa")) {
            JDBCVersionManager manager = new JDBCVersionManager(conn);
            manager.getCurrentVersion("test");
        }
    }

    @Test
    public void testFindCurrentVersionIsEmptyWhenVersionsTableIsMissing() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test-findCurrentVersion-missing-table", "sa", "sa")) {
            JDBCVersionManager manager = new JDBCVersionManager(conn);

            assertEquals(Optional.empty(), manager.findCurrentVersion("test"));
        }
    }

    @Test
    public void testFindCurrentVersionReturnsVersionWhenVersionsTableIsPresent() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test-findCurrentVersion-present-table", "sa", "sa")) {
            JDBCVersionManager manager = new JDBCVersionManager(conn);
            manager.initialize();
            manager.updateVersion("test", "1.0.0");

            assertEquals(Optional.of("1.0.0"), manager.findCurrentVersion("test"));
        }
    }
}
