package base;

import base.players.RealPlayer;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import java.util.*;

public class BuyingProblemSolver {

    private RealPlayer client;
    private Building card;
    private MPSolver solver;

    public BuyingProblemSolver(RealPlayer client, Building card) {
        Loader.loadNativeLibraries();
        System.out.println("Problème linéaire du coût de " + card);
        this.client = client;
        this.card = card;
        this.solver = MPSolver.createSolver("SCIP");
    }

    public boolean minBuyingCost() {//TODO:utiliser les ressources que possèdent les voisins au début du tour
        MPObjective objective = solver.objective();
        objective.setMinimization();
        for (String required : card.getCost().keySet()) {
            double left_price = resourcePrice(required, "left");
            double right_price = resourcePrice(required, "right");
            double left_stock = client.getNeighbours(client.getHost().getPlayers().indexOf(client)).get(0).findResourceStock(required);
            double right_stock = client.getNeighbours(client.getHost().getPlayers().indexOf(client)).get(1).findResourceStock(required);
            double nb_required = card.getCost().get(required) - client.getProxy().findResourceStock(required);
            //on fait une contrainte si on a besoin d'acheter de la ressource et qu'il est possible d'en acheter aux voisins
            if (nb_required > 0 && (left_stock > 0 || right_stock > 0)) {
                MPConstraint ct = solver.makeConstraint(nb_required, Double.POSITIVE_INFINITY, required);
                if (left_stock > 0) {
                    MPVariable left = solver.makeIntVar(0., left_stock, "left_" + required);
                    ct.setCoefficient(left, 1.);
                    objective.setCoefficient(left, left_price);
                }
                if (right_stock > 0) {
                    MPVariable right = solver.makeIntVar(0, right_stock, "right_" + required);
                    ct.setCoefficient(right, 1.);
                    objective.setCoefficient(right, right_price);
                }
            }
        }
        System.out.println(this);
        final MPSolver.ResultStatus resultStatus = solver.solve();
        System.out.println(resultStatus);
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Objective value = " + objective.value());
        }
        return resultStatus == MPSolver.ResultStatus.OPTIMAL;
    }

    public double resourcePrice(String resource, String side) {
        if (resource.equals("glass") || resource.equals("fabric") || resource.equals("papyrus")) {
            return client.getReductions().get("manufactured_" + side) ? 1. : 2.;
        } else {
            return client.getReductions().get("raw_" + side) ? 1. : 2.;
        }
    }

    public int[] getCosts() {
        Map<String, Double> left_order = new HashMap<>();
        Map<String, Double> right_order = new HashMap<>();
        for (MPVariable var : solver.variables()) {
            if (var.name().contains("left")) {
                left_order.put(var.name().split("_")[1], var.solutionValue());
            } else {
                right_order.put(var.name().split("_")[1], var.solutionValue());
            }
        }
        System.out.println(left_order);
        System.out.println(right_order);
        int left_cost = 0;
        int right_cost = 0;
        for (String item : left_order.keySet()) {
            left_cost += resourcePrice(item, "left") * left_order.get(item);
        }
        for (String item : right_order.keySet()) {
            right_cost += resourcePrice(item, "right") * right_order.get(item);
        }
        return new int[]{left_cost, right_cost};
    }

    public String toString() {
        String res = "Minimize ";
        for (MPVariable var : solver.variables()) {
            res += solver.objective().getCoefficient(var) + " " + var.name();
            if (!(var.name().equals(solver.variables()[solver.variables().length-1].name()))) {
                res += " + ";
            }
        }
        res += "\nsubject to\n";
        for (MPConstraint ct : solver.constraints()) {
            for (MPVariable ct_var : solver.variables()) {
                if (ct.getCoefficient(ct_var) > 0) {
                    res += ct_var.name();
                    if (!(ct_var.name().equals(solver.variables()[solver.variables().length-1].name()))) {
                        res += " + ";
                    }
                }
            }
            res += " >= " + ct.lb() + "\n";
        }
        return res;
    }
}
