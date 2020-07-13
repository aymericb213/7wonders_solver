package base.effects;

import base.Player;
import base.effects.Effect;

public class Reduction implements Effect {

    private String type;
    private String side;

    public Reduction(String type, String side) {
        this.type = type;
        this.side = side;
    }

    @Override
    public void applyEffect(Player target) {
        target.getReductions().put(type + "_" + side, true);
        System.out.println("Réduction de coût pour " + type + " à " + side);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return "Reduction{" +
                "type='" + type + '\'' +
                ", side='" + side + '\'' +
                '}';
    }
}
