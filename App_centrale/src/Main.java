import java.sql.*;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws SQLException {
        AppCentrale app = new AppCentrale();
        int choix;

        System.out.println("-------------------------------------------------------");
        System.out.println("--------------MENU APPLICATION CENTRALE----------------");
        System.out.println("-------------------------------------------------------");

        do {
            System.out.println("1- Ajouter un cours");
            System.out.println("2- Ajouter un étudiant");
            System.out.println("3- Inscrire un étudiant à un cours");
            System.out.println("4- Créer un projet pour un cours");
            System.out.println("5- Créer des groupes pour un projet");
            System.out.println("6- Visualiser les cours");
            System.out.println("7- Visualiser tous les projets");
            System.out.println("8- Visualiser toutes les compositions de groupe d'un projet");
            System.out.println("9- Valider un groupe");
            System.out.println("10- Valider tous les groupes d'un projet");
            System.out.println();
            System.out.print("Entrez votre choix: ");
            choix = Integer.parseInt(scanner.nextLine());

            switch (choix) {
                case 1:
                    app.ajouterUnCours();
                    break;
                case 2:
                    app.ajouterUnEtudiant();
                    break;
                case 3:
                    app.inscrireEtudiantUnCours();
                    break;
                case 4:
                    app.creerUnProjetPourUnCours();
                    break;
                case 5:
                    app.creerGroupePourUnProjet();
                    break;
                case 6:
                    app.afficherCours();
                    break;
                case 7:
                    app.afficherProjets();
                    break;
                case 8:
                    app.afficherCompositionsGroupePourUnProjet();
                    break;
                case 9:
                    app.validerUnGroupe();
                    break;
                case 10:
                    app.validerTousLesGroupesDUnProjet();
                    break;
            }
        } while (choix >= 1 && choix <= 10);
    }



}
