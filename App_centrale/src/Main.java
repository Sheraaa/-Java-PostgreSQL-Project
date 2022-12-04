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
                    afficherCours();
                    break;
                case 7:
                    afficherProjets();
                    break;
                case 8:
                    afficherCompositionsGroupePourUnProjet();
                    break;
                case 9:
                    validerUnGroupe();
                    break;
                case 10:
                    validerTousLesGroupesDUnProjet();
                    break;
            }
        } while (choix >= 1 && choix <= 10);
    }


    private static Connection connexionDatabase() {
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
            System.out.println(se.getMessage());
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
            System.out.println(se.getMessage());
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
            System.exit(1);
        }
    }

    private static void creerUnProjetPourUnCours() {
        System.out.println("---------Créer un projet pour un cours-------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.inserer_projets(?,?,?,?,?)");
            String valeur;
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
            System.exit(1);
        }
    }

    private static void afficherCours() {
        System.out.println("---------------Afficher les cours-------------------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM logiciel.afficher_cours");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          ");
            }
            System.out.println();
            System.out.println("----------------------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getString(1) + "                 \t" + rs.getString(2)
                        + "             \t" + rs.getString(3));
            }
            System.out.println("----------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }

    private static void afficherProjets() {
        System.out.println("---------Afficher les projets-------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM logiciel.afficher_projets");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 1; i < resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          ");
            }
            System.out.println();
            while (rs.next()) {
                System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3)
                        + "\t" + rs.getString(4) + "\t" + rs.getString(5));
            }
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }

    private static void afficherCompositionsGroupePourUnProjet() {
        System.out.println("---------Afficher composition de groupe d'un projet-------------");
        Connection conn = connexionDatabase();
        try {
            System.out.print("Entrez l'identifiant de projet: ");
            String idProjet = scanner.nextLine();
            idProjet = scanner.nextLine();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM logiciel.afficher_composition_groupe");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "         ");
            }
            System.out.println();

          //  System.out.println(rs.getString(1));
            //TODO Filtrer l'affichage que pour idProjet
            //&& rs.getString(1).equals(idProjet)
            while (rs.next() ) {
                System.out.println(rs.getInt(1) + "         " + rs.getString(2)
                        + "         " + rs.getString(3)
                        + "         " + rs.getBoolean(4) + "         " + rs.getBoolean(5));
            }
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }

    private static void validerUnGroupe() {
        System.out.println("---------Valider un groupe d'un projet -------------");
        Connection conn = connexionDatabase();
        try {
            System.out.print("Entrez le numéro du groupe: ");
            int idGroupe = scanner.nextInt();
            System.out.print("Entrez l'identifiant du projet: ");
            String idProjet = scanner.nextLine();
            idProjet = scanner.nextLine();
            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.valider_un_groupe(?,?)");
            ps.setString(1, idProjet);
            ps.setInt(2, idGroupe);
            ps.executeQuery();

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }

    private static void validerTousLesGroupesDUnProjet() {
        System.out.println("---------Valider tous les groupes d'un projet -------------");
        Connection conn = connexionDatabase();
        try {
            System.out.print("Entrez l'identifiant du projet: ");
            String idProjet = scanner.nextLine();
            idProjet = scanner.nextLine();
            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.valider_tous_les_groupes(?)");
            ps.setString(1, idProjet);
            ps.executeQuery();

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }

}
