package base.players;

import base.Building;
import base.BuyingProblemSolver;
import base.Game;
import base.Wonder;
import base.effects.Effect;
import strategies.*;

import java.util.*;

public class RealPlayer implements Player {

    private Game host;
    private PlayerProxy proxy;
    private Wonder wonder;
    private Set<Building> buildings;
    private Map<String,Integer> resources;
    private Map<String,Integer> dual_resources;
    private Map<String, Boolean> reductions;
    private int[] buying_costs;

    private int defeats;
    private int nb_discarded;
    private List<Building> hand;
    private Queue<Effect> pending;

    private boolean bonus_turn;
    private int free_building;
    private PlayerStrategy playstyle;



    public RealPlayer(Game host) {
        this.host = host;
        this.buildings = new HashSet<>();
        this.resources = new HashMap<>();
        resources.put("gold", 3);
        resources.put("shield", 0);
        resources.put("vp", 0);
        this.dual_resources = new HashMap<>();
        this.reductions = new HashMap<>();
        reductions.put("raw_left", false);
        reductions.put("raw_right", false);
        reductions.put("manufactured_left", false);
        reductions.put("manufactured_right", false);
        this.buying_costs = new int[]{0,0};

        this.defeats = 0;
        this.hand = new ArrayList<>();
        this.pending = new LinkedList<>();
        this.bonus_turn = false;
        this.free_building = 0;
        this.playstyle = new NaiveBuild(this);

        this.proxy = new PlayerProxy(this);
    }
    
    public RealPlayer(Game host, PlayerStrategy playstyle) {
        this(host);
        this.playstyle = playstyle;
    }

    public void play() {
        playstyle.play();
    }

    /* Actions */

    /**
     * Construit un bâtiment.
     * @param card Le bâtiment à construire.
     */
    public void build(Building card) {
        System.out.println("Construction de " + card);
        hand.remove(card);
        buildings.add(card);
        card.getEffects().forEach(e -> e.applyEffect(this));
        if (!isFree(card)) {
            payGoldCost(card);
        }
    }

    /**
     * Construit une étape de merveille.
     * @param card La carte utilisée pour la construction.
     */
    public void buildWonderStageWith(Building card) {
        System.out.println("Etape de merveille avec " + card);
        hand.remove(card);
        Building wonder_stage = wonder.getStages().get(wonder.getBuiltStages());
        wonder_stage.getEffects().forEach(e -> e.applyEffect(this));
        payGoldCost(wonder_stage);
        wonder.setBuiltStages(wonder.getBuiltStages() + 1);
    }

    /**
     * Met le bâtiment sélectionné dans la défausse du jeu.
     * @param card La carte à défausser.
     */
    public void discard(Building card) {
        System.out.println("Défaussage de " + card + "\n");
        host.getDiscarded().add(card);
        hand.remove(card);
        resources.put("gold", resources.get("gold")+3);
        nb_discarded++;
    }

    public void payGoldCost(Building card) {
        if (card.getCost().get("gold") != null) {//coût en or du bâtiment
            resources.put("gold", resources.get("gold") - card.getCost().get("gold"));
        }
        if (buying_costs[0] + buying_costs[1] > 0) {//coût d'achat de ressources
            System.out.println("shopping");
            int index = host.getPlayers().indexOf(this);
            List<PlayerProxy> neighbours = getNeighbours(index);
            neighbours.get(0).giveResourceTo(neighbours.get(0).getResources(), "gold", buying_costs[0]);
            neighbours.get(1).giveResourceTo(neighbours.get(1).getResources(), "gold", buying_costs[1]);
            resources.put("gold", resources.get("gold") - (buying_costs[0] + buying_costs[1]));
            buying_costs = new int[]{0,0};
        }
    }

    /**
     * Vérifie que le joueur peut construire un bâtiment donné.
     * @param card La carte à construire.
     * @return Le résultat du test.
     */
    public boolean canBuild(Building card) {
        if (buildings.contains(card)) {
            return false;
        }
        if (isFree(card)) {
            return true;
        }
        int card_gold_cost = card.getCost().get("gold") == null ? 0 : card.getCost().get("gold");
        if (card_gold_cost > resources.get("gold")) {//si le joueur n'a pas assez d'or, inutile de tester les autres ressources
            System.out.println("pas assez d'or");
            return false;
        }
        int diff;
        int raw = resources.get("raw") == null ? 0 : resources.get("raw");
        int manufactured = resources.get("manufactured") == null ? 0 : resources.get("manufactured");
        for (String required : card.getCost().keySet()) {
            diff = card.getCost().get(required) - proxy.findResourceStock(required);//calcul de base
            if (diff > 0 && (raw>0 || manufactured>0)) {//ressources "joker", passage unique par wrapper
                if (required.equals("wood") || required.equals("stone") || required.equals("clay") || required.equals("ore")) {
                    while (diff>0 || raw>0) {
                        diff -= 1;
                        raw -= 1;
                    }
                } else if (required.equals("glass") || required.equals("fabric") || required.equals("papyrus")) {
                    while (diff>0 || manufactured>0) {
                        diff -= 1;
                        manufactured -= 1;
                    }
                }
            }
            if  (free_building > 0 && diff > 0) {//capacité de la statue de Zeus, si activée
                free_building--;
                break;
            }
            if (diff > 0) {//achat chez les voisins
                BuyingProblemSolver pb = new BuyingProblemSolver(this, card);
                boolean resolved = pb.minBuyingCost();
                if (resolved) {//ressources disponibles, il faut vérifier que le joueur peut payer
                    buying_costs = pb.getCosts();
                    return (buying_costs[0] > 0 && buying_costs[1] > 0) && (card_gold_cost + buying_costs[0] + buying_costs[1] <= resources.get("gold"));
                } else {//les voisins ne peuvent pas fournir les ressources manquantes
                    System.out.println(card.getName() + " : pas assez de " + required);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isFree(Building card) {
        for (Building b : buildings) {
            if (b.getName().equals(card.getLinkedBuilding())) {//test de doublon
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne les voisins du joueur.
     * @param player_index L'indice du joueur dont on veut connaître les voisins.
     * @return Un tableau dont la première case contient le voisin de gauche et la seconde celui de droite.
     */
    public List<PlayerProxy> getNeighbours(int player_index) {
        List<RealPlayer> players = host.getPlayers();
        if (player_index == 0) {//début de liste
            return new ArrayList<>(Arrays.asList(players.get(players.size() - 1).getProxy(), players.get(player_index + 1).getProxy()));
        } else if (player_index == players.size()-1) {//fin de liste
            return new ArrayList<>(Arrays.asList(players.get(player_index - 1).getProxy(), players.get(0).getProxy()));
        } else {//cas général
            return new ArrayList<>(Arrays.asList(players.get(player_index - 1).getProxy(), players.get(player_index + 1).getProxy()));
        }
    }

    public int play_count() {
        return this.getBuildings().size() + this.getNbDiscarded() + this.getWonder().getBuiltStages();
    }

    @Override
    public void giveResourceTo(Map<String, Integer> stock, String resource, int num) {
        stock.put(resource, stock.get(resource) + num);
    }

    public Game getHost() {
        return host;
    }

    public void setHost(Game host) {
        this.host = host;
    }

    public PlayerProxy getProxy() {
        return proxy;
    }

    public void setProxy(PlayerProxy proxy) {
        this.proxy = proxy;
    }

    public Wonder getWonder() {
        return this.wonder;
    }

    public void setWonder(Wonder wonder) {
        this.wonder = wonder;
    }

    public Set<Building> getBuildings() {
        return this.buildings;
    }

    public void setBuildings(Set<Building> buildings) {
        this.buildings = buildings;
    }

    public Map<String,Integer> getResources() {
        return this.resources;
    }

    public void setResources(Map<String,Integer> resources) {
        this.resources = resources;
    }

    public Map<String, Integer> getDuals() {
        return dual_resources;
    }

    public void setDuals(Map<String, Integer> dual_resources) {
        this.dual_resources = dual_resources;
    }

    public Map<String, Boolean> getReductions() {
        return reductions;
    }

    public void setReductions(Map<String, Boolean> reductions) {
        this.reductions = reductions;
    }

    public int[] getBuyingCosts() {
        return buying_costs;
    }

    public void setBuyingCosts(int[] buying_costs) {
        this.buying_costs = buying_costs;
    }

    public int getDefeats() {
        return defeats;
    }

    public void setDefeats(int defeats) {
        this.defeats = defeats;
    }

    public List<Building> getHand() {
        return this.hand;
    }

    public void setHand(List<Building> hand) {
        this.hand = hand;
    }

    public Queue<Effect> getPending() {
        return pending;
    }

    public void setPending(Queue<Effect> pending) {
        this.pending = pending;
    }

    public boolean bonusTurn() {
        return this.bonus_turn;
    }

    public void setBonusTurn(boolean bonus_turn) {
        this.bonus_turn = bonus_turn;
    }

    public int freeBuilding() {
        return this.free_building;
    }

    public void setFreeBuilding(int free_building) {
        this.free_building = free_building;
    }

    public PlayerStrategy getPlaystyle() {
        return this.playstyle;
    }

    public void setPlaystyle(PlayerStrategy playstyle) {
        this.playstyle = playstyle;
    }

    public int getNbDiscarded() {
        return nb_discarded;
    }

    public void setNbDiscarded(int nb_discarded) {
        this.nb_discarded = nb_discarded;
    }

    public String shortString() {
        return "\n" + wonder.shortString() + "\n" + play_count() + "\n" + resources + "\nBâtiments :" + buildings;
    }

    public String toString() {
        return "\n" + wonder.shortString() + "\n" + play_count() + "\nCartes défaussées : " + nb_discarded + "\n" + resources + "\n" + dual_resources + "\nMain :" + hand;
    }
}
