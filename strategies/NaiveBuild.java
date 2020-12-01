package strategies;

import base.Building;
import base.players.RealPlayer;
import base.Wonder;

public class NaiveBuild implements PlayerStrategy {

  private RealPlayer client;

  public NaiveBuild(RealPlayer client) {
    this.client = client;
  }

  @Override
  public void play() {
    Wonder wonder = client.getWonder();
    if (wonder.getBuiltStages() != wonder.getStages().size() && client.canBuild(wonder.getStages().get(wonder.getBuiltStages()))) {
      client.buildWonderStageWith(client.getHand().get(0));//construction de merveille en priorité
    } else {
      boolean built = false;
      for (Building card : client.getHand()) {//on cherche un bâtiment que l'on peut construire
        if (client.canBuild(card)) {
          client.build(card);
          built=true;
          break;
        }
      }
      if (!built) {
        client.discard(client.getHand().get(0));//défausse en dernier recours
      }
    }
  }
}
