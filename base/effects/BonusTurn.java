package base.effects;

import base.players.RealPlayer;

public class BonusTurn implements Effect {

    public BonusTurn() {
    }

    @Override
    public void applyEffect(RealPlayer target) {
        System.out.println("Tour supplémentaire");
        target.setBonusTurn(true);
    }
}
