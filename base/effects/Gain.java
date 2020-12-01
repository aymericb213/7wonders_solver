package base.effects;

import base.players.RealPlayer;

import java.util.Map;

public class Gain implements Effect {

    protected Map<String,Integer> gain;

    public Gain(Map<String, Integer> gain) {
        this.gain = gain;
    }

    @Override
    public void applyEffect(RealPlayer target) {
        Map<String, Integer> target_resources;
        for (String key : gain.keySet()) {
            target_resources = key.contains("/") ? target.getProxy().getDualResources() : target.getProxy().getResources();
            target.getProxy().giveResourceTo(target_resources, key, gain.get(key));
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
