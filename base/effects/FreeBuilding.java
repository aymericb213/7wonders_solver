package base.effects;

import base.players.RealPlayer;

public class FreeBuilding implements Effect {

    public FreeBuilding() {
    }

    @Override
    public void applyEffect(RealPlayer target) {
        target.setFreeBuilding(4 - target.getHost().getAge());
        System.out.println("Bâtiments gratuits");
    }

    @Override
    public String toString() {
        return "FreeBuilding";
    }
}
