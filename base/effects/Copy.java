package base.effects;

import base.*;
import java.util.*;

public class Copy implements Effect {

    public Copy() {}

    @Override
    public void applyEffect(Player target) {
        if (!(target.getHost().isEndgame())) {//mise en attente de la copie
            System.out.println("Copie");
            target.getPending().offer(this);
        } else {
            //Listing des guildes disponibles pour la copie
            List<Building> guilds = new ArrayList<>();
            for (Player neighbor : target.getHost().getNeighbours(target.getHost().getPlayers().indexOf(target))) {
                for (Building b : neighbor.getBuildings()) {
                    if (b.getType().equals("guild")) {
                        guilds.add(b);
                    }
                }
            }

            //Estimation de la meilleure guilde à copier
            int original_vp_value = target.getResources().get("vp");
            Building best_guild = null;
            int best_value = 0;
            for (Building guild : guilds) {
                for (Effect e : guild.getEffects()) {
                    e.applyEffect(target);
                }
                int guild_vp = target.getResources().get("vp") - original_vp_value;
                if (guild.getName().contains("scientifique")) {//calcul spécial pour la guilde des scientifiques
                    int max_science = target.getHost().scienceScore();
                    target.getResources().put("symbol", target.getResources().get("symbol") -1);
                    guild_vp = max_science - target.getHost().scienceScore();
                } else {
                    target.getResources().put("vp", original_vp_value);
                }
                System.out.println("Gain de " + guild_vp + " points avec " + guild);
                if (guild_vp > best_value) {
                    best_value = guild_vp;
                    best_guild = guild;
                }
            }
            System.out.println("Meilleure guilde : " + best_guild + " avec " + best_value);
            if (!(best_guild == null)) {
                for (Effect e : best_guild.getEffects()) {
                    e.applyEffect(target);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Copy";
    }
}
