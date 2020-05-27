package base.effects;

import base.Player;

import java.util.Map;

public class Gain implements Effect {

    protected Map<String,Integer> gain;

    public Gain(Map<String, Integer> gain) {
        this.gain = gain;
    }

    @Override
    public void applyEffect(Player target) {
        Map<String, Integer> target_resources;
        for (String key : gain.keySet()) {
            target_resources = key.contains("/") ? target.getDualResources() : target.getResources();
            int value_to_update = target_resources.get(key) == null ? 0 : target_resources.get(key);
            target_resources.put(key, value_to_update + gain.get(key));
        }
    }

    public Map<String, Integer> getGain() {
        return gain;
    }

    public void setGain(Map<String, Integer> gain) {
        this.gain = gain;
    }

    @Override
    public String toString() {
        return "Gain{" +
                "gain=" + gain +
                '}';
    }
}
