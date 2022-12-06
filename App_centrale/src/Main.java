import java.sql.*;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    private static String url = "jdbc:postgresql://localhost:5432/logiciel";
    //private static String url="jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani";
    private static Connection conn;
    private static PreparedStatement ps1;
    private static PreparedStatement ps2;
    private static PreparedStatement ps3;
    private static PreparedStatement ps4;
    private static PreparedStatement ps5;
    private static PreparedStatement ps6;
    private static PreparedStatement ps7;
    private static PreparedStatement ps8;
    private static PreparedStatement ps9;
    private static PreparedStatement ps10;
    private static PreparedStatement ps11;


    public Main() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }

        try {
            //conn=DriverManager.getConnection(url,”dbchehrazadouazzani”,”SQINPAG0B”);
            conn = DriverManager.getConnection(url, "postgres", "shera");
        } catch (SQLException e) {
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }
        try {
            preparedStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    public void preparedStatement() throws SQLException {
        ps1 = conn.prepareStatement("INSERT INTO logiciel.cours(code_cours, nom, bloc, nombre_credits) VALUES (?,?,?,?)");
        ps2 = conn.prepareStatement("INSERT INTO logiciel.etudiants(nom,prenom,mail,pass_word) VALUES (?,?,?,?)");
        ps3 = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours(?,?)");
   //     ps4 = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(?,?,?)");
        ps5 = conn.prepareStatement("SELECT logiciel.inserer_projets(?,?,?,?,?)");
        ps6 = conn.prepareStatement("SELECT logiciel.creer_groupes(?,?,?)");
        ps7 = conn.prepareStatement("SELECT * FROM logiciel.afficher_cours");
        ps8 = conn.prepareStatement("SELECT * FROM logiciel.afficher_projets");
        ps9 = conn.prepareStatement("SELECT * FROM logiciel.afficher_composition_groupe");
        ps10 = conn.prepareStatement("SELECT logiciel.valider_un_groupe(?,?)");
        ps11 = conn.prepareStatement("SELECT logiciel.valider_tous_les_groupes(?)");
    }

    public static void main(String[] args) throws SQLException {
        Main main = new Main();
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

    private static void ajouterUnCours() throws SQLException {
        System.out.println("---------ajouter un cours-------------");

        String valeur;
        int nbValeur;

        System.out.print("Entrez le code du cours(BINV****): ");
        valeur = scanner.nextLine();
        valeur = scanner.nextLine();
        ps1.setString(1, valeur);

        System.out.print("Entrez le nom du cours: ");
        valeur = scanner.nextLine();
        ps1.setString(2, valeur);

        System.out.print("Entrez le bloc: ");
        nbValeur = scanner.nextInt();
        ps1.setInt(3, nbValeur);

        System.out.print("Entrez le nombre de crédits: ");
        nbValeur = scanner.nextInt();
        ps1.setInt(4, nbValeur);
        ps1.executeUpdate();
        System.out.println("--------> Insertion REUSSI !");

    }

    private static void ajouterUnEtudiant() throws SQLException {
        System.out.println("---------ajouter un etudiant-------------");

        String valeur;
        System.out.print("Entrez le nom de l'étudiant: ");
        valeur = scanner.nextLine();
        valeur = scanner.nextLine();
        ps2.setString(1, valeur);
        System.out.print("Entrez le prénom de l'étudiant: ");
        valeur = scanner.nextLine();
        ps2.setString(2, valeur);
        System.out.print("Entrez le mail de l'étudiant: ");
        valeur = scanner.nextLine();
        ps2.setString(3, valeur);
        System.out.print("Entrez le mot de passe de l'étudiant: ");
        valeur = scanner.nextLine();

        //crypter password
        String gensel = BCrypt.gensalt();
        ps2.setString(4, BCrypt.hashpw(valeur, gensel));
        ps2.executeUpdate();
        System.out.println("--------> Insertion REUSSI !  <---------");

    }

    private static void inscrireEtudiantUnCours() throws SQLException {
        System.out.println("---------Inscrire l'étudiant à un cours-------------");

        String valeur;
        System.out.print("Entrez le mail de l'étudiant: ");
        valeur = scanner.nextLine();
        valeur = scanner.nextLine();
        ps3.setString(1, valeur);
        System.out.print("Entrez le code du cours(BINV****): ");
        valeur = scanner.nextLine();
        ps3.setString(2, valeur);
        ps3.executeQuery();
        System.out.println("--------> Insertion REUSSI !<---------------------");

    }

    private static void creerUnProjetPourUnCours() throws SQLException {
        System.out.println("---------Créer un projet pour un cours-------------");

        String valeur;
        System.out.print("Entrez l'identifiant du projet: ");
        valeur = scanner.nextLine();
        valeur = scanner.nextLine();
        ps5.setString(1, valeur);
        System.out.print("Entrez le nom du projet: ");
        valeur = scanner.nextLine();
        ps5.setString(2, valeur);
        System.out.print("Entrez la date de début du projet (AAAA-MM-JJ): ");
        valeur = scanner.nextLine();
        ps5.setDate(3, java.sql.Date.valueOf(valeur));
        System.out.print("Entrez la date de fin du projet (AAAA-MM-JJ): ");
        valeur = scanner.nextLine();
        ps5.setDate(4, java.sql.Date.valueOf(valeur));
        System.out.print("Entrez le code du cours (BINV****): ");
        valeur = scanner.nextLine();
        ps5.setString(5, valeur);
        ps5.executeQuery();
        System.out.println("--------> Insertion REUSSI !");

    }

    private static void creerGroupePourUnProjet() throws SQLException {
        System.out.println("---------Créer des groupes pour un projet-------------");

        String valeur;
        int val;
        System.out.print("Entrez l'identifiant du projet: ");
        valeur = scanner.nextLine();
        valeur = scanner.nextLine();
        ps6.setString(1, valeur);
        System.out.print("Combien de groupe: ");
        val = scanner.nextInt();
        ps6.setInt(2, val);
        System.out.print("Entrez la taille du groupe: ");
        val = scanner.nextInt();
        ps6.setInt(3, val);
        ps6.executeQuery();
        System.out.println("--------> Insertion REUSSI !");
    }

    private static void afficherCours() throws SQLException {
        System.out.println("---------------Afficher les cours-------------------------");

        ResultSet rs = ps7.executeQuery();
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

    }

    private static void afficherProjets() throws SQLException {
        System.out.println("---------Afficher les projets-------------");

        ResultSet rs = ps8.executeQuery();
        ResultSetMetaData resultSetMetaData = rs.getMetaData();

        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            System.out.print(resultSetMetaData.getColumnName(i) + "\t\t\t\t");
        }
        System.out.println();
        while (rs.next()) {
            System.out.println(rs.getString(1) + "\t\t\t\t" + rs.getString(2) + "\t\t\t\t" + rs.getString(3)
                    + "\t\t\t\t" + rs.getString(4) + "\t\t\t\t" + rs.getString(5)
                    + "\t\t\t\t" + rs.getString(6));
        }
    }

    private static void afficherCompositionsGroupePourUnProjet() throws SQLException {
        System.out.println("---------Afficher composition de groupe d'un projet-------------");

        System.out.print("Entrez l'identifiant de projet: ");
        String idProjet = scanner.nextLine();
        idProjet = scanner.nextLine();

        ResultSet rs = ps9.executeQuery();
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            System.out.print(resultSetMetaData.getColumnName(i) + "         ");
        }
        System.out.println();

        //  System.out.println(rs.getString(1));
        //TODO Filtrer l'affichage que pour idProjet
        //&& rs.getString(1).equals(idProjet)
        while (rs.next()) {
            System.out.println(rs.getInt(1) + "         " + rs.getString(2)
                    + "         " + rs.getString(3)
                    + "         " + rs.getBoolean(4) + "         " + rs.getBoolean(5));
        }
    }

    private static void validerUnGroupe() throws SQLException {
        System.out.println("---------Valider un groupe d'un projet -------------");

        System.out.print("Entrez le numéro du groupe: ");
        int idGroupe = scanner.nextInt();
        System.out.print("Entrez l'identifiant du projet: ");
        String idProjet = scanner.nextLine();
        idProjet = scanner.nextLine();

        ps10.setString(1, idProjet);
        ps10.setInt(2, idGroupe);
        ps10.executeQuery();

    }

    private static void validerTousLesGroupesDUnProjet() throws SQLException {
        System.out.println("---------Valider tous les groupes d'un projet -------------");

        System.out.print("Entrez l'identifiant du projet: ");
        String idProjet = scanner.nextLine();
        idProjet = scanner.nextLine();

        ps11.setString(1, idProjet);
        ps11.executeQuery();

    }

}
