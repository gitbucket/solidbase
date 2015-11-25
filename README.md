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
    <id>amateras</id>
    <name>Project Amateras Maven2 Repository</name>
    <url>http://amateras.sourceforge.jp/mvn/</url>
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
  <changeSet id="1.0.0" author="Naoki Takezoe">
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
  "test",
  new Version("1.0.0", new LiquibaseMigration("test_1.0.0.xml")),
  new Version("1.0.1", new LiquibaseMigration("test_1.0.1.xml")),
  ...
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
