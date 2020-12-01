package strategies;

import base.Building;
import base.players.RealPlayer;

import java.util.Random;

public class FullDiscard implements PlayerStrategy {

    private RealPlayer client;

    public FullDiscard(RealPlayer client) {
        this.client = client;
    }

    @Override
    public void play() {
        Building card = client.getHand().get(new Random().nextInt(client.getHand().size()));
        client.discard(card);
    }

}
