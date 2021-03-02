# Solidbase [![build](https://github.com/gitbucket/solidbase/workflows/build/badge.svg?branch=master)](https://github.com/gitbucket/solidbase/actions?query=workflow%3Abuild+branch%3Amaster) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.gitbucket/solidbase/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.gitbucket/solidbase) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/gitbucket/solidbase/blob/master/LICENSE)

Generic migration tool for RDBMS and other resources based on [Liquibase](http://www.liquibase.org/).

- Multi-database (based on Liquibase)
- Multi-resource (not only RDBMS)
- Multi-module (modules can have each versions)

## Usage

### Add dependency

Add following dependency into your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>io.github.gitbucket</groupId>
    <artifactId>solidbase</artifactId>
    <version>1.0.3</version>
  </dependency>
</dependencies>
```

### Define migration

Create Liquibase migration xml files under `src/main/resources`. For example:

- `src/main/resources/test_1.0.0.xml`

  ```xml
  <changeSet>
    <createTable tableName="person">
        <column name="id" type="int" autoIncrement="true" primaryKey="true" nullable="false"/>
        <column name="firstname" type="varchar(50)"/>
        <column name="lastname" type="varchar(50)" constraints nullable="false"/>
        <column name="state" type="char(2)"/>
    </createTable>
  </changeSet>
  ```

Define migration that migrates RDBMS using these XML files:

```java
import io.github.gitbucket.solidbase.migration.LiquibaseMigration;
import io.github.gitbucket.solidbase.model.Module;
import io.github.gitbucket.solidbase.model.Version;

Module module = new Module(
  // module id
  "test",
  // versions (oldest first)
  new Version("1.0.0", new LiquibaseMigration("test_1.0.0.xml")),
  new Version("1.0.1", new LiquibaseMigration("test_1.0.1.xml")),
  ...
);
```

You can add migration for resources other than RDBMS by implementing `Migration` interface. Added migrations are executed in order.

```java
import io.github.gitbucket.solidbase.migration.LiquibaseMigration;
import io.github.gitbucket.solidbase.migration.Migration;

new Version("1.0.0",
  // At first, migrate RDBMS
  new LiquibaseMigration("test_1.0.0.xml"),
  // Second, migrate other resources
  new Migration(){
    @Override
    public void migrate(String moduleId, String version, Map<String, Object> context) throws Exception {
      ...
    }
  }
);
```

### Run

Then, run migration as below:

```java
import io.github.gitbucket.solidbase.SolidBase;
import java.sql.DriverManager;
import liquibase.database.core.H2Database;

Solidbase solidbase = new Solidbase();

solidbase.migrate(
  DriverManager.getConnection("jdbc:h2:mem:test", "sa", "sa"),
  Thread.currentThread().getContextClassLoader(),
  new H2Database(),
  module
);
```

Defferences between the current version and the latest version are applied.

### Background

Solidbase creates a following `VERSIONS` table to manage versions automatically:

Column name    | Data type    | Not Null
:--------------|:-------------|:---------
MODULE_ID (PK) | VARCHAR(100) | Yes
VERSION        | VARCHAR(100) | Yes

Solidbase uses this table to know the current version. When migration of the new version is successful, it updates the version with the new version.

## Migration

### XML migration

`LiquibaseMigration` migrates the database by Liquibase like XML as above.

XML schema is improved from Liquibase to be possible to declare column information as attributes instead of nested elements. And a default variable `${currentDateTime}` is available in the XML:

```xml
<insert tableName="person">
  <column name="firstname" value="root"/>
  <column name="lastname" value="root"/>
  <column name="registeredDate" valueDate="${currentDateTime}"/>
</insert>
```

### SQL migration

`SqlMigration` migrates the database by native SQL.

### Apply RDBMS specific configuration
In the default, `LiquibaseMigration` and `SqlMigration` try to load a file from classpath as following order:

1. Specified path with `_${database}` name suffix (if specified)
2. Specified path (if specified)
3. `${moduleId}_${version}_${database}.${extension}`
4. `${moduleId}_${version}.${extension}`

It's possible to apply different XML/SQL for each databases by creating multiple files such as `gitbucket_1.0.0_h2.sql` (for H2 database) and `gitbucket_1.0.0_mysql.sql` (for MySQL).
