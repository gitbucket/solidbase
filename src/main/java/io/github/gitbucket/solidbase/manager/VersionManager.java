package io.github.gitbucket.solidbase.manager;

public interface VersionManager {

    void initialize() throws Exception;

    void updateVersion(String moduleId, String version) throws Exception;

    String getCurrentVersion(String moduleId) throws Exception;

}
