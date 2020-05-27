package base;

import java.util.*;

public class Main {

	public static void main(String[] args) {
        Scanner sc= new Scanner(System.in);
        System.out.println("Nombre de joueurs : ");
        int nb_players = Integer.parseInt(sc.nextLine());
        if (nb_players > 2 && nb_players < 8) {
            Game game = new Game(nb_players);
            System.out.println("\n" + game + "\n");
            game.play();
        } else {
            System.out.println("Le jeu nécessite de 3 à 7 joueurs");
        }
	}
}
