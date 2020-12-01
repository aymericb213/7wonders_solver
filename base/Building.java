package base;

import base.effects.Effect;

import java.util.*;

public class Building {

  private String name;
  private String type;
  private Map<String,Integer> cost;
  private List<Effect> effects;
  private String linked_building;

  public Building(String name, String type, Map<String,Integer> cost, List<Effect> effects, String linked_building) {
    this.name = name;
    this.type = type;
    this.cost = cost;
    this.effects = effects;
    this.linked_building = linked_building;
  }

  @Override
  public boolean equals(Object obj) {
      if (obj == this) {
          return true;
      }
      if (!(obj instanceof Building)) {
          return false;
      }
      Building b = (Building) obj;
      return this.name.equals(b.getName());
  }

  @Override
  public int hashCode() {
      int code = 9;
      code = 37 * code + (this.name != null ? this.name.hashCode() : 0);
      return code;
  }
  
  public String getName() {
	return this.name;
  }

  public void setName(String name) {
	this.name = name;
  }

  public String getType() {
		return this.type;
	}

  public void setType(String type) {
		this.type = type;
	}

  public Map<String,Integer> getCost() {
		return this.cost;
	}

  public void setCost(Map<String,Integer> cost) {
		this.cost = cost;
	}

  public List<Effect> getEffects() { return this.effects;}

  public void setEffects(List<Effect> effects) {this.effects = effects;}

  public String getLinkedBuilding() {return this.linked_building;}

  public void setLinkedBuilding(String linked_building) {this.linked_building = linked_building;}

  public String toString() {
    return name + " (" + type + ")";
  }
}
