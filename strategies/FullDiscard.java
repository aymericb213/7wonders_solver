package strategies;

import base.Building;
import base.Player;

import java.util.Random;

public class FullDiscard implements PlayerStrategy {

    private Player client;

    public FullDiscard(Player client) {
        this.client = client;
    }

    @Override
    public void play() {
        Building card = client.getHand().get(new Random().nextInt(client.getHand().size()));
        client.discard(card);
    }

}
