package base.effects;

import base.Building;
import base.players.RealPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscardSearch implements Effect {

    private RealPlayer owner;
    private int priority;

    public DiscardSearch(int priority) {
        this.priority = priority;
    }

    @Override
    public void applyEffect(RealPlayer target) {
        owner = target;
        System.out.println("Choix d'une carte défaussée");
        target.getHost().getDiscardSearches().offer(this);
    }

    public void pendingEffect() {      //TODO: smart build choice
        List<Building> discard = new ArrayList<>(owner.getHost().getDiscarded());
        if (discard.size() > 0) {
            for (Building built : owner.getBuildings()) {
                String match = built.getName();
                discard.removeIf(b -> b.getName().regionMatches(0, match, 0, match.length() - 1));
            }
        }
        if (discard.size() > 0) {
            Collections.shuffle(discard);
            Building b = discard.get(0);
            owner.getBuildings().add(b);
            owner.getHost().getDiscarded().remove(b);
            System.out.println("Construction depuis la défausse");
        }
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
