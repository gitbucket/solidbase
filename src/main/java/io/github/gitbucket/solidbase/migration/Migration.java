package io.github.gitbucket.solidbase.migration;

import java.util.Map;

/**
 * Define an interface of migration.
 * <p>
 * Solidbase provides {@link LiquibaseMigration} as default implementation of migration.
 * Of course, you can also make your own migration by implementing this interface.
 * </p>
 */
public interface Migration {

    void migrate(String moduleId, String version, Map<String, Object> context) throws Exception;

}
