package base.players;

import java.util.Map;

public interface Player {

    Map<String,Integer> getResources();

    Map<String,Integer> getDuals();

    void giveResourceTo(Map<String, Integer> stock, String resource, int num);

}
