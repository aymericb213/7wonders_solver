package base.effects;

import base.Player;

public class BonusTurn implements Effect {

    public BonusTurn() {
    }

    @Override
    public void applyEffect(Player target) {
        System.out.println("Tour supplémentaire");
        target.setBonusTurn(true);
    }
}
