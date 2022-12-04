import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int choix;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }

        String url = "jdbc:postgresql://localhost:5432/logiciel";
        // String url="jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani“  <-- A MODIFIER
        Connection conn = null;

        try {
            //conn=DriverManager.getConnection(url,”dbchehrazadouazzani”,”SQINPAG0B”);
            conn = DriverManager.getConnection(url, "postgres", "shera");
        } catch (SQLException e) {
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }

        System.out.println("-------------------------------------------------------");
        System.out.println("--------------MENU APPLICATION CENTRALE----------------");
        System.out.println("-------------------------------------------------------");
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

        do {
            System.out.print("Entrez votre choix: ");
            choix = scanner.nextInt();

            switch (choix) {
                case 1:
                    ajouterUnCours();
                    break;
                case 2:
                    ajouterUnEtudiant();
                    break;
                case 3:
                    inscrireEtudiantUnCours();
                    break;
                case 4:
                    creerUnProjetPourUnCours();
                    break;
                case 5:
                    creerGroupePourUnProjet();
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    break;
                case 10:
                    break;
            }
        } while (choix >= 1 && choix <= 10);
    }




    private static Connection connexionDatabase(){
        String url = "jdbc:postgresql://localhost:5432/logiciel";
        // String url="jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani“  <-- A MODIFIER
        Connection conn = null;


        try {
            //conn=DriverManager.getConnection(url,”dbchehrazadouazzani”,”SQINPAG0B”);
            conn = DriverManager.getConnection(url, "postgres", "shera");
        } catch (SQLException e) {
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }
        return conn;
    }

    private static void ajouterUnCours() {
        System.out.println("---------ajouter un cours-------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO " +
                    "logiciel.cours(code_cours, nom, bloc, nombre_credits) VALUES (?,?,?,?)");
            String valeur;
            int nbValeur;

            System.out.print("Entrez le code du cours(BINV****): ");
            valeur = scanner.nextLine();
            valeur = scanner.nextLine();
            ps.setString(1, valeur);
            System.out.println(valeur);

            System.out.print("Entrez le nom du cours: ");
            valeur = scanner.nextLine();
            ps.setString(2, valeur);

            System.out.print("Entrez le bloc: ");
            nbValeur = scanner.nextInt();
            ps.setInt(3, nbValeur);

            System.out.print("Entrez le nombre de crédits: ");
            nbValeur = scanner.nextInt();
            ps.setInt(4, nbValeur);
            ps.executeUpdate();
            System.out.println("--------> Insertion REUSSI !");
        } catch (SQLException se) {
            System.out.println("Erreur lors de l’insertion !");
            se.printStackTrace();
            System.exit(1);
        }
    }
    private static void ajouterUnEtudiant() {
        System.out.println("---------ajouter un etudiant-------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO " +
                    "logiciel.etudiants(nom,prenom,mail) VALUES (?,?,?)");
            String valeur;
            System.out.print("Entrez le nom de l'étudiant: ");
            valeur = scanner.nextLine();
            valeur = scanner.nextLine();
            ps.setString(1, valeur);
            System.out.print("Entrez le prénom de l'étudiant: ");
            valeur = scanner.nextLine();
            ps.setString(2, valeur);
            System.out.print("Entrez le mail de l'étudiant: ");
            valeur = scanner.nextLine();
            ps.setString(3, valeur);
            ps.executeUpdate();
            System.out.println("--------> Insertion REUSSI !");
        } catch (SQLException se) {
            System.out.println("Erreur lors de l’insertion !");
            se.printStackTrace();
            System.exit(1);
        }
    }
    private static void inscrireEtudiantUnCours() {
        System.out.println("---------Inscrire l'étudiant à un cours-------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("select logiciel.inscrire_etudiant_cours(?,?)");
            String valeur;
            System.out.print("Entrez le mail de l'étudiant: ");
            valeur = scanner.nextLine();
            valeur = scanner.nextLine();
            ps.setString(1, valeur);
            System.out.print("Entrez le code du cours(BINV****): ");
            valeur = scanner.nextLine();
            ps.setString(2, valeur);
            ps.executeQuery();
            System.out.println("--------> Insertion REUSSI !");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            se.printStackTrace();
            System.exit(1);
        }
    }
    private static void creerUnProjetPourUnCours() {
        System.out.println("---------Créer un projet pour un cours-------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.inserer_projets(?,?,?,?,?)");
            String valeur;
            int val;
            System.out.print("Entrez l'identifiant du projet: ");
            valeur = scanner.nextLine();
            valeur = scanner.nextLine();
            ps.setString(1, valeur);
            System.out.print("Entrez le nom du projet: ");
            valeur = scanner.nextLine();
            ps.setString(2, valeur);
            System.out.print("Entrez la date de début du projet (AAAA-MM-JJ): ");
            valeur = scanner.nextLine();
            ps.setDate(3, java.sql.Date.valueOf(valeur));
            System.out.print("Entrez la date de fin du projet (AAAA-MM-JJ): ");
            valeur = scanner.nextLine();
            ps.setDate(4, java.sql.Date.valueOf(valeur));
            System.out.print("Entrez le code du cours (BINV****): ");
            valeur = scanner.nextLine();
            ps.setString(5, valeur);
            ps.executeQuery();
            System.out.println("--------> Insertion REUSSI !");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            se.printStackTrace();
            System.exit(1);
        }
    }
    private static void creerGroupePourUnProjet() {
        System.out.println("---------Créer des groupes pour un projet-------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.creer_groupes(?,?,?)");
            String valeur;
            int val;
            System.out.print("Entrez l'identifiant du projet: ");
            valeur = scanner.nextLine();
            valeur = scanner.nextLine();
            ps.setString(1, valeur);
            System.out.print("Entrez le nombre du groupe: ");
            val = scanner.nextInt();
            ps.setInt(2, val);
            System.out.print("Entrez la taille du groupe: ");
            val = scanner.nextInt();
            ps.setInt(3, val);
            ps.executeQuery();
            System.out.println("--------> Insertion REUSSI !");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            se.printStackTrace();
            System.exit(1);
        }
    }

}
