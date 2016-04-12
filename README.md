# Solidbase [![Build Status](https://travis-ci.org/gitbucket/solidbase.svg?branch=master)](https://travis-ci.org/gitbucket/solidbase)

Generic migration tool for RDBMS and other resources based on [Liquibase](http://www.liquibase.org/).

- Multi-database (based on Liquibase)
- Multi-resource (not only RDBMS)
- Multi-module (modules can have each versions)

## Usage

### Add dependency

Add following dependency into your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>amateras-snapshot</id>
    <name>Project Amateras Maven2 Repository</name>
    <url>http://amateras.sourceforge.jp/mvn-snapshot/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>io.github.gitbucket</groupId>
    <artifactId>solidbase</artifactId>
    <version>1.0.0-SNAPSHOT</version>
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

Deferences between the current version and the latest version are applied.

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

In the default, `SqlMigration` try to load a SQL file from classpath as following order:

1. Specified path (if specified)
2. `${moduleId}_${version}_${database}.sql`
3. `${moduleId}_${version}.sql`

It's possible to apply different SQL for each databases by creating multiple SQL files for the same version such as `gitbucket_1.0.0_h2.sql` (for H2 database) and `gitbucket_1.0.0_mysql.sql` (for MySQL).

