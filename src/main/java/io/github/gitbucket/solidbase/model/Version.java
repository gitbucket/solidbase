package io.github.gitbucket.solidbase.model;

import io.github.gitbucket.solidbase.migration.Migration;

import java.util.ArrayList;
import java.util.List;

public class Version {

    private final String version;
    private final List<Migration> migrations = new ArrayList<>();

    public Version(String version){
        this.version = version;
    }

    public Version(String version, List<Migration> migrations){
        this.version = version;
        this.migrations.addAll(migrations);
    }

    public Version(String version, Migration... migrations){
        this.version = version;
        for(Migration migration: migrations){
            this.migrations.add(migration);
        }
    }

    public String getVersion(){
        return this.version;
    }

    public List<Migration> getMigrations(){
        return this.migrations;
    }

}
