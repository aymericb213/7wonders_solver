package base.players;

import java.util.HashMap;
import java.util.Map;

public class PlayerProxy implements Player {

    private final RealPlayer client;
    private Map<String, Integer> actual_resources;
    private Map<String, Integer> actual_dual;
    private Map<String, Integer> usable_duals;

    public PlayerProxy(RealPlayer client) {
        this.client = client;
        this.actual_resources = client.getResources();
        this.actual_dual = new HashMap<>(client.getDuals());
        this.usable_duals = new HashMap<>(client.getDuals());
    }

    public int findResourceStock(String resource) {
        int available = client.getResources().get(resource) == null ? 0 : client.getResources().get(resource);
        for (String dual_resource : usable_duals.keySet()) {//ressources à double type
            if (usable_duals.get(dual_resource) > 0 && dual_resource.contains(resource)) {
                available += 1;
                usable_duals.put(dual_resource, usable_duals.get(dual_resource) - 1);
            }
        }
        return available;
    }

    @Override
    public Map<String, Integer> getResources() {
        return actual_resources;
    }

    public Map<String, Integer> getDualResources() {
        return actual_dual;
    }

    @Override
    public Map<String, Integer> getDuals() {
        return usable_duals;
    }

    public void giveResourceTo(Map<String, Integer> stock, String resource, int num) {
        int value_to_update = stock.get(resource) == null ? 0 : stock.get(resource);
        stock.put(resource, value_to_update + num);
    }

    public void updateClientResources() {
        client.setResources(new HashMap<>(actual_resources));
        client.setDuals(new HashMap<>(actual_dual));
        usable_duals = new HashMap<>(actual_dual);
    }

    public RealPlayer getClient() {
        return client;
    }


}
