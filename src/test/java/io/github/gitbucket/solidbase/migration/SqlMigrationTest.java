package io.github.gitbucket.solidbase.migration;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class SqlMigrationTest {

    @Test
    public void testSplitMultiStatementSql1(){
        SqlMigration migration = new SqlMigration();

        List<String> result = migration.splitMultiStatementSql("UPDATE USERS SET FLG = 1");
        assertEquals(1, result.size());
        assertEquals("UPDATE USERS SET FLG = 1", result.get(0));
    }

    @Test
    public void testSplitMultiStatementSql2(){
        SqlMigration migration = new SqlMigration();

        List<String> result = migration.splitMultiStatementSql("UPDATE USERS SET FLG = 1; UPDATE USERS SET FLG = 2;");
        assertEquals(2, result.size());
        assertEquals("UPDATE USERS SET FLG = 1", result.get(0));
        assertEquals("UPDATE USERS SET FLG = 2", result.get(1));
    }
}
