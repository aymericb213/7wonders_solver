package base.effects;

import base.Building;
import base.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GainByCopy extends Gain {

    private String check_range;//self, neighbors ou all
    private String type;

    public GainByCopy(Map<String, Integer> gain, String check_range, String type) {
        super(gain);
        this.check_range = check_range;
        this.type = type;
    }

    @Override
    public void applyEffect(Player target) {
        int copies_of_type=0;
        List<Building> checklist = new ArrayList<>();
        if (check_range.equals("self") || check_range.equals("all")) {//bâtiments du joueur
            checklist = target.getBuildings();
        }
        if (check_range.equals("neighbors") || check_range.equals("all")) {//bâtiments des voisins
            for (Player neighbor : target.getHost().getNeighbours(target.getHost().getPlayers().indexOf(target))) {
                checklist.addAll(neighbor.getBuildings());
            }
        }
        for (Building b : checklist) {//dénombrement des bâtiments du type concerné
            if (b.getType().equals(type)) {
                copies_of_type++;
            }
        }
        for (String key : gain.keySet()) {
            if (!target.getHost().isEndgame() && key.equals("vp")) {
                HashMap<String, Integer> vp_gain_by_copy = new HashMap<>();
                vp_gain_by_copy.put(key, gain.get(key));
                GainByCopy pending_gain = new GainByCopy(vp_gain_by_copy, check_range, type);
                target.getPending().offer(pending_gain);//mise en attente des gains de points pour le calcul final
            } else {
                HashMap<String, Integer> final_gain = new HashMap<>();
                final_gain.put(key, gain.get(key)*copies_of_type);
                Gain final_effect = new Gain(final_gain);
                final_effect.applyEffect(target);
            }
        }
    }

    public String getCheck_range() {
        return check_range;
    }

    public void setCheck_range(String check_range) {
        this.check_range = check_range;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "GainByCopy{" + super.toString() +
                ", check_range='" + check_range + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
