package base.effects;

import base.Game;
import base.Player;

public class FreeBuilding implements Effect {

    public FreeBuilding() {
    }

    @Override
    public void applyEffect(Player target) {
        target.setFreeBuilding(4 - target.getHost().getAge());
        System.out.println("Bâtiments gratuits");
    }

    @Override
    public String toString() {
        return "FreeBuilding";
    }
}
