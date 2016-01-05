# Solidbase

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
        <column name="id" type="int" autoIncrement="true">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="firstname" type="varchar(50)"/>
        <column name="lastname" type="varchar(50)">
            <constraints nullable="false"/>
        </column>
        <column name="state" type="char(2)"/>
    </createTable>
  </changeSet>
  ```

Define migration that migrates RDBMS using these XML files:

```java
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
