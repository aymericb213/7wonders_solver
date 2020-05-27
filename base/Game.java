package base;

import base.effects.DiscardSearch;
import base.effects.Effect;
import data.JsonGameDataReader;

import java.util.*;

public class Game {
    /* Matières premières : wood, stone, clay, ore / wrapper : raw
    Produits manufacturés : glass, fabric, papyrus / wrapper : manufactured
    Symboles scientifiques : gear, tablet, compass / wrapper : symbol
    Attributs joueur : vp, shield, gold
    Effets : gain, reduction, discard_search, free_building, copy, bonus_turn */
  private String id;
  private List<Building> age_1_deck;
  private List<Building> age_2_deck;
  private List<Building> age_3_deck;
  private List<Wonder> wonders;

  private List<Player> players;
  private int age;
  private boolean endgame;
  private List<Building> discarded;
  private PriorityQueue<DiscardSearch> discard_searches;

  public Game(int number_of_players) {
      this.id = UUID.randomUUID().toString();
      this.age_1_deck = new ArrayList<>();
      this.age_2_deck = new ArrayList<>();
      this.age_3_deck = new ArrayList<>();
      this.wonders = new ArrayList<>();
      this.players = new ArrayList<>(number_of_players);
      this.endgame = false;
      this.age = 1;
      this.discarded = new ArrayList<>();
      this.discard_searches = new PriorityQueue<>((first, second) -> {
          int value_first = first.getPriority();
          int value_second = second.getPriority();
          if (value_first > value_second) {
              return -1;
          } else if (value_first == value_second) {
              return 0;
          } else {
              return 1;
          }
      });
      init(number_of_players);
  }

  /** Crée l'état initial de la partie avec un nombre de joueurs donné, disposant chacun d'une merveille et d'une main.
    * @param number_of_players Le nombre de participants dans la partie en cours.
    */
  public void init(int number_of_players) {
      System.out.println("Initialisation");
    //Création des joueurs
    for (int i = 1 ; i < number_of_players + 1; i++) {
      players.add(new Player(this));
    }

    //Chargement des cartes utilisées dans la partie
    JsonGameDataReader reader = new JsonGameDataReader(this, "data/base.json");
    reader.buildDecks(number_of_players);

    for (int j = 0; j < number_of_players; j++) {
      //Attribution de la merveille
      Collections.shuffle(wonders);
      Wonder attributed = wonders.get(0);
      players.get(j).setWonder(attributed);
      players.get(j).getResources().put(attributed.getBaseResource(), 1);//ressource de base de la merveille
      //Les deux faces de la merveille attribuée sont retirées des tirages suivants
      String match = players.get(j).getWonder().getName();
      wonders.removeIf(w -> w.getName().regionMatches(0, match, 0, match.length() - 1));
    }
    fillHands(age_1_deck);//Distribution de la main de départ
    System.out.println(players);
  }

  /** Joue la partie et affiche le résultat.*/
  public void play() {
     int turn = 1;
     while (age<3 || !(players.get(0).getHand().isEmpty())) {
         System.out.println("Tour " + turn);
         for (Player p : players) {
             System.out.println(p.shortString());
             System.out.println("Main : " + p.getHand());
             p.play();
         }
         /*System.out.println("Défausse avant : " + discarded);
         while (!discard_searches.isEmpty()) {
             discard_searches.poll().buildFromDiscard();
         }
         System.out.println("Défausse après : " + discarded);*/
         nextTurn();
         turn++;
     }
  }

  /** Effectue le changement de tour en faisant tourner les mains des joueurs. */
  public void nextTurn() {
      List<Building> tmp_hand;
      if (age%2 != 0) {//rotation horaire pendant les âges 1 et 3
          tmp_hand = players.get(players.size()-1).getHand();
          for (int odd=players.size()-1; odd > 0; odd--) {
              players.get(odd).setHand(players.get(odd-1).getHand());
          }
          players.get(0).setHand(tmp_hand);
      } else {//rotation antihoraire pendant l'âge 2
          tmp_hand = players.get(0).getHand();
          for (int even=0; even < players.size()-1; even++) {
              players.get(even).setHand(players.get(even+1).getHand());
          }
          players.get(players.size()-1).setHand(tmp_hand);
      }
      if (players.get(0).getHand().size()==1) {
          for (Player p : players) {
              if (p.bonusTurn()) {//tour supplémentaire
                  p.play();
                  p.setBonusTurn(false);
              } else {//cas général
                  discarded.addAll(p.getHand());
                  p.getHand().clear();
              }
          }
          nextAge();
      }
  }

  /** Effectue le changement d'âge, avec résolution des conflits militaires et distribution d'une nouvelle main. */
  public void nextAge() {
      solveMilitaryConflicts();
      age++;
      System.out.println("Passage à l'âge " + age);
      if (age==2) {
          fillHands(age_2_deck);
      } else if (age==3) {
          fillHands(age_3_deck);
      } else {
          this.endgame = true;
          computeScores();//calcul du score final et désignation du vainqueur
      }
  }

  /** Donne une main complète à chaque joueur provenant du paquet donné.
    * @param deck Le paquet à distribuer.
    */
  public void fillHands(List<Building> deck) {
      Collections.shuffle(deck);
      while (!(deck.isEmpty())) {
          for (Player p : players) {
             p.getHand().add(deck.get(0));
             deck.remove(0);
          }
      }
  }

  /**
    * Modifie les scores de chaque joueur en fonction des conflits militaires avec ses voisins.
    */
  public void solveMilitaryConflicts() {
     int vp_gain;//gain en cas de victoire militaire
     switch (age) {
         case 1:
             vp_gain=1;
             break;
         case 2:
             vp_gain=3;
             break;
         case 3:
             vp_gain=5;
             break;
         default:
             throw new IllegalArgumentException("L'âge n'est pas valide");
     }
     //Résolution des conflits
     for (int i = 0; i < players.size(); i=i+2) {
        Player[] enemies = getNeighbours(i);
        Player p = players.get(i);
        for (Player enemy : enemies) {
            //si le nombre de joueurs est impair, on ignore le dernier calcul de la boucle car il est le même que le premier
            if (players.size()%2 != 0 && i==players.size()-1 && enemy.equals(enemies[1])) {
                break;
            }
            Map<String, Integer> player_resources = p.getResources();
            Map<String, Integer> enemy_resources = enemy.getResources();
            if (player_resources.get("shield") > enemy_resources.get("shield")) {//victoire
               player_resources.put("vp", player_resources.get("vp")+vp_gain);
               enemy_resources.put("vp", enemy_resources.get("vp")-1);
               enemy.setDefeats(enemy.getDefeats()+1);
            } else if (player_resources.get("shield") < enemy_resources.get("shield")) {//défaite
                player_resources.put("vp", player_resources.get("vp")-1);
                enemy_resources.put("vp", enemy_resources.get("vp")+vp_gain);
                p.setDefeats(p.getDefeats()+1);
            }
        }
     }
     System.out.println("Conflits résolus");
  }

  public void computeScores() {
      System.out.println("\nPartie terminée");
      System.out.println("\nDéfausse : \n" + discarded + "\n");
      for (Player p : players) {
          while (!p.getPending().isEmpty()) {//ajout des points dépendant de l'état final du jeu
              p.getPending().poll().applyEffect(p);
          }
          System.out.println(p + "\nBâtiments : " + p.getBuildings());
      }
  }

    /**
     * Retourne les voisins du joueur donné.
     * @param player_index L'indice du joueur dont on veut connaître les voisins.
     * @return Un tableau dont la première case contient le voisin de gauche et la seconde celui de droite.
     */
  public Player[] getNeighbours(int player_index) {
     Player[] res = new Player[2];
     if (player_index==0) {//début de liste
        res[0]= players.get(players.size()-1);
        res[1]= players.get(player_index+1);
     } else if (player_index==players.size()-1) {//fin de liste
        res[0]= players.get(player_index-1);
        res[1]= players.get(0);
     } else {//cas général
        res[0]= players.get(player_index-1);
        res[1]= players.get(player_index+1);
     }
     return res;
  }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Building> getAge_1_deck() {
        return age_1_deck;
    }

    public void setAge_1_deck(List<Building> age_1_deck) {
        this.age_1_deck = age_1_deck;
    }

    public List<Building> getAge_2_deck() {
        return age_2_deck;
    }

    public void setAge_2_deck(List<Building> age_2_deck) {
        this.age_2_deck = age_2_deck;
    }

    public List<Building> getAge_3_deck() {
        return age_3_deck;
    }

    public void setAge_3_deck(List<Building> age_3_deck) {
        this.age_3_deck = age_3_deck;
    }

    public List<Wonder> getWonders() {
        return wonders;
    }

    public void setWonders(List<Wonder> wonders) {
        this.wonders = wonders;
    }

    public List<Player> getPlayers() {
    return this.players;
  }

    public void setPlayers(List<Player> players) {
    this.players = players;
  }

	public int getAge() {
		return this.age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public List<Building> getDiscarded() {
		return this.discarded;
	}

	public void setDiscarded(List<Building> discarded) {
		this.discarded = discarded;
	}

    public PriorityQueue<DiscardSearch> getDiscardSearches() {
        return discard_searches;
    }

    public void setDiscardSearches(PriorityQueue<DiscardSearch> discard_searches) {
        this.discard_searches = discard_searches;
    }

    public boolean isEndgame() {
        return endgame;
    }

    public void setEndgame(boolean endgame) {
        this.endgame = endgame;
    }

	public String toString() {
		return "Game " + id + "\n" + players.size() + " joueurs\nAge " + age;
	}


}