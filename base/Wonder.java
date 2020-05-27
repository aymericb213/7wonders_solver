package base;

import java.util.*;

public class Wonder {

    private String name;
    private String base_resource;
    private List<Building> stages;
    private int built_stages;

    public Wonder(String name, String base_resource, List<Building> stages) {
        this.name = name;
        this.base_resource = base_resource;
        this.stages = stages;
        this.built_stages = 0;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseResource() {
        return this.base_resource;
    }

    public void setBase_resource(String base_resource) {
        this.base_resource = base_resource;
    }

    public List<Building> getStages() {
        return this.stages;
    }

    public void setStages(List<Building> stages) {
        this.stages = stages;
    }

    public int getBuiltStages() {
        return this.built_stages;
    }

    public void setBuiltStages(int built_stages) {
        this.built_stages = built_stages;
    }

    public String shortString() {
        return name + " " + built_stages;
    }

    public String toString() {
        return "Wonder [name=" + name + ", base_resource=" + base_resource + ", stages=" + stages + ", built_stages=" + built_stages + "]";
    }
}
