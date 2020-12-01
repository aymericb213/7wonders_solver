package base.effects;

import base.*;
import base.players.PlayerProxy;
import base.players.RealPlayer;

import java.util.*;

public class Copy implements Effect {

    public Copy() {}

    @Override
    public void applyEffect(RealPlayer target) {
        if (!(target.getHost().isEndgame())) {//mise en attente de la copie
            System.out.println("Copie");
            target.getPending().offer(this);
        } else {
            //Listing des guildes disponibles pour la copie
            List<Building> guilds = new ArrayList<>();
            for (PlayerProxy neighbor : target.getNeighbours(target.getHost().getPlayers().indexOf(target))) {
                for (Building b : neighbor.getClient().getBuildings()) {
                    if (b.getType().equals("guild")) {
                        guilds.add(b);
                    }
                }
            }

            //Estimation de la meilleure guilde à copier
            Building best_guild = null;
            int best_value = 0;
            for (Building guild : guilds) {
                guild.getEffects().forEach(e -> e.applyEffect(target));
                int guild_vp = Math.abs(target.getResources().get("vp") - target.getProxy().getResources().get("vp"));
                if (guild.getName().contains("scientifique")) {//calcul spécial pour la guilde des scientifiques
                    int max_science = target.getHost().scienceScore(target, 0);//TODO:corriger copie guilde scientifique
                    target.getProxy().getResources().put("symbol", target.getProxy().getResources().get("symbol") -1);
                    guild_vp = max_science - target.getHost().scienceScore(target, 0);
                }
                System.out.println("Gain de " + guild_vp + " points avec " + guild);
                if (guild_vp > best_value) {
                    best_value = guild_vp;
                    best_guild = guild;
                }
            }
            System.out.println("Meilleure guilde : " + best_guild + " avec " + best_value);
            if (!(best_guild == null)) {
                best_guild.getEffects().forEach(e -> e.applyEffect(target));
            }
        }
    }

    @Override
    public String toString() {
        return "Copy";
    }
}
