package io.github.gitbucket.solidbase.manager;

import java.util.Optional;

public interface VersionManager {

    void initialize() throws Exception;

    void updateVersion(String moduleId, String version) throws Exception;

    String getCurrentVersion(String moduleId) throws Exception;

    Optional<String> findCurrentVersion(String moduleId) throws Exception;

}
