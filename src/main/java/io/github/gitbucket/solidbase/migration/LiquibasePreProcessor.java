package io.github.gitbucket.solidbase.migration;

public interface LiquibasePreProcessor {

    String[] CONSTRAINT_PROPERTIES = {
        "nullable", "primaryKey", "primaryKeyName", "unique", "uniqueConstraintName", "references", "foreignKeyName", "deleteCascade", "deferrable", "initiallyDeferred"
    };

    String preProcess(String moduleId, String version, String source) throws Exception;

}
