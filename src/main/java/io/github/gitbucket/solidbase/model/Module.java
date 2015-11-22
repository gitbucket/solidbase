package io.github.gitbucket.solidbase.model;

import java.util.ArrayList;
import java.util.List;

public class Module {

    private String moduleId;
    private List<Version> versions = new ArrayList<>();

    public Module(String moduleId){
        this.moduleId = moduleId;
    }

    public Module(String moduleId, List<Version> versions){
        this.moduleId = moduleId;
        this.versions.addAll(versions);
    }

    public String getModuleId(){
        return this.moduleId;
    }

    public List<Version> getVersions(){
        return this.versions;
    }

}
