package data;

import java.io.File;
import java.io.IOException;
import java.util.*;

import base.*;
import base.effects.*;
import com.squareup.moshi.*;
import okio.Okio;

public class JSONGameDataReader {

  private Game client;
  private String filepath;

  public JSONGameDataReader(Game g, String filepath) {
    this.client = g;
    this.filepath = filepath;
  }

  public class EffectAdapter {

      @ToJson
      public String toJson(Effect effect) {
          if (effect instanceof GainByCopy) {
              return "gain:" + "(" + ((GainByCopy) effect).getCheck_range() + "-"+ ((GainByCopy) effect).getType() +")"+((Gain) effect).getGain().toString();
          }
          if (effect instanceof Gain) {
              return "gain:" + ((Gain) effect).getGain().toString();
          }
          if (effect instanceof Reduction) {
              return "reduction:" + ((Reduction) effect).getType() + "," + ((Reduction) effect).getSide();
          }
          if (effect instanceof DiscardSearch) {
              return "discard_search:" + ((DiscardSearch) effect).getPriority();
          }
          if (effect instanceof FreeBuilding) {
              return "free_building";
          }
          if (effect instanceof Copy) {
              return "copy";
          }
          if (effect instanceof BonusTurn) {
              return "bonus_turn";
          }
          throw new JsonDataException("Effect type not recognized");
      }

      @FromJson
      public Effect fromJson(String effect) {
          String[] attributes = effect.split(":");
          switch (attributes[0]) {
              case "gain" :
                  HashMap<String,Integer> gain = new HashMap<>();
                  String[] options = attributes[1].replaceAll(".*\\(|\\).*", "").split("-");
                  String target = null;
                  String type = null;
                  if (!(options.length==1)) {
                     target = options[0];
                     type = options[1];
                  }
                  String[] pairs = attributes[1].replaceAll(".*\\{|\\}.*","").split(", ");
                  for (String s : pairs) {
                      String[] pair = s.split("=");
                      gain.put(pair[0], Integer.parseInt(pair[1]));
                  }
                  return target!=null ? new GainByCopy(gain,target,type) : new Gain(gain);
              case "reduction" :
                  String[] args = attributes[1].split(",");
                  return new Reduction(args[0],args[1]);
              case "discard_search" :
                  return new DiscardSearch(Integer.parseInt(attributes[1]));
              case "free_building" :
                  return new FreeBuilding();
              case "copy" :
                  return new Copy();
              case "bonus_turn" :
                  return new BonusTurn();
              default:
                  throw new JsonDataException("Effect type not recognized : " + attributes[0]);
          }
      }
  }

  public class BuildingAdapter {

      @FromJson
      public ArrayList<Building> fromJson(JsonReader reader) {
          ArrayList<Building> res = new ArrayList<>();
          Map<String,Integer> cost = new HashMap<>() ;
          List<Effect> effects=new ArrayList<>();
          String name="";
          String type="";
          String linked_building="";
          int copies=0;
          try {
              reader.beginObject();
              while (reader.hasNext()) {
                  switch (reader.nextName()) {
                      case "cost" :
                          Map<String, Double> map = (Map)reader.readJsonValue();
                          for (String key : map.keySet()) {
                              int converted_value = map.get(key).intValue();
                              cost.put(key, converted_value);
                          }
                          break;
                      case "copies" :
                          String s = (String)reader.readJsonValue();
                          for (String nb : s.split(",")) {
                              if (client.getPlayers().size() >= Integer.parseInt(nb)) {
                                  copies++;
                              }
                          }
                          break;
                      case "effects" :
                          ArrayList<String> effect_strings = (ArrayList<String>)reader.readJsonValue();
                          for (String effect_name : effect_strings) {
                              effects.add(new EffectAdapter().fromJson(effect_name));
                          }
                          break;
                      case "name" :
                          name = (String)reader.readJsonValue();
                          break;
                      case "linked_building" :
                          linked_building = (String)reader.readJsonValue();
                          break;
                      case "type" :
                          type = (String)reader.readJsonValue();
                          break;
                      default:
                          throw new JsonDataException();
                  }
              }
              reader.endObject();
              for (int i=0; i < copies; i++) {
                  res.add(new Building(name, type, cost, effects, linked_building));
              }
              return res;
          } catch (IOException e) {
              e.printStackTrace();
              return null;
          }
      }
  }

    /**
     * Lit un fichier JSON et remplit les paquets de jeu d'une partie en fonction du nombre de joueurs.
     * @param nb_players Le nombre de joueurs de la partie.
     */
  public void buildDecks(int nb_players) {
      Moshi moshi = new Moshi.Builder().add(new EffectAdapter()).add(new BuildingAdapter()).build();
      JsonAdapter<Building> buildingAdapter = moshi.adapter(Building.class);
      JsonAdapter<Wonder> wonderAdapter = moshi.adapter(Wonder.class);
      try {
          JsonReader reader = JsonReader.of(Okio.buffer(Okio.source(new File(filepath))));
          reader.beginObject();//début de la lecture
          while (reader.hasNext()) {
             String name = reader.nextName();
             reader.beginArray();
             switch (name) {
                 case "wonders":
                     fillDeck(reader, client.getWonders(), wonderAdapter);
                     break;
                 case "first":
                     fillDeck(reader, client.getAge_1_deck());
                     break;
                 case "second":
                     fillDeck(reader, client.getAge_2_deck());
                     break;
                 case "third":
                     fillDeck(reader, client.getAge_3_deck());
                     break;
                 case "guilds":
                     //on récupère les guildes séparément, puisqu'elles ne seront pas toutes ajoutées au deck age 3
                     ArrayList<Building> all_guilds = new ArrayList<>();
                     fillDeck(reader, all_guilds, buildingAdapter);
                     Collections.shuffle(all_guilds);
                     for (int i=0; i<nb_players+2; i++) {//on met nb_players+2 guildes dans le deck age 3
                         client.getAge_3_deck().add(all_guilds.get(i));
                     }
                     break;
                 default:
                     throw new JsonDataException("Objet inconnu dans le fichier JSON : " + name);
             }
             reader.endArray();
          }
          reader.endObject();//lecture terminée
          System.out.println("Lecture de " + filepath + " terminée avec succès");
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

    /**
     * Remplit un paquet de cartes.
     * @param reader Le lecteur JSON utilisé pour récupérer les cartes.
     * @param array_to_fill Le paquet à remplir.
     * @param adapter L'adapteur Moshi du type de carte du paquet.
     */
  public void fillDeck(JsonReader reader, List array_to_fill, JsonAdapter adapter) {
      try {
          while (reader.hasNext()) {
                  array_to_fill.add(adapter.fromJson(reader));
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

    /**
     * Utilisée pour les cartes avec plusieurs exemplaires possibles.
     * @param reader Le lecteur JSON utilisé pour récupérer les cartes.
     * @param array_to_fill Le paquet à remplir.
     */
  public void fillDeck(JsonReader reader, List array_to_fill) {
      try {
          while (reader.hasNext()) {
                  array_to_fill.addAll(new BuildingAdapter().fromJson(reader));
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

    public Game getClient() {
        return client;
    }

    public void setClient(Game client) {
        this.client = client;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String toString() {
      return "Lecteur du fichier " + filepath + "pour la partie " + client.getId();
    }
}
