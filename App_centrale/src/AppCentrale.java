import java.sql.*;
import java.util.Scanner;

public class AppCentrale {
    private PreparedStatement psDemo;
    private PreparedStatement ps1;
    private PreparedStatement ps2;
    private PreparedStatement ps3;
    private PreparedStatement ps4;
    private PreparedStatement ps5;
    private PreparedStatement ps6;
    private PreparedStatement ps7;
    private PreparedStatement ps8;
    private PreparedStatement ps9;
    private PreparedStatement ps10;
    private PreparedStatement ps11;
    private Connection conn;
    private String url = "jdbc:postgresql://localhost:5432/logiciel";
    //private static String url="jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani";
    private static Scanner scanner = new Scanner(System.in);

    /**
     * connect to postgresql and prepare statements
     */
    public AppCentrale() {
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
        prepareStatements();
        requeteDemo();
    }

    private void prepareStatements() {
        try {
            ps1 = conn.prepareStatement("SELECT logiciel.inserer_cours(?,?,?,?)");
            ps2 = conn.prepareStatement("SELECT logiciel.inserer_etudiant(?,?,?,?)");
            ps3 = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours(?,?)");
            ps4 = conn.prepareStatement("SELECT logiciel.inserer_projets(?,?,?,?,?)");
            ps5 = conn.prepareStatement("SELECT logiciel.creer_groupes(?,?,?)");
            ps6 = conn.prepareStatement("SELECT * FROM logiciel.afficher_cours");
            ps7 = conn.prepareStatement("SELECT * FROM logiciel.afficher_projets");
            ps8 = conn.prepareStatement("SELECT * FROM logiciel.afficher_composition_groupe");
            ps9 = conn.prepareStatement("SELECT logiciel.valider_un_groupe(?,?)");
            ps10 = conn.prepareStatement("SELECT logiciel.valider_tous_les_groupes(?)");
            ps11 = conn.prepareStatement("SELECT logiciel.chercher_id_projet(?)");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void ajouterUnCours() throws SQLException {
        System.out.println("---------ajouter un cours-------------");

        String valeur;
        int nbValeur;

        System.out.print("Entrez le code du cours(BINV****): ");
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
        try {
            ps1.executeQuery();
            System.out.println("--------> Insertion du cours REUSSI ! <------------");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de l'insertion  du cours! <------------");
            System.out.println();
        }
    }

    public void ajouterUnEtudiant() throws SQLException {
        System.out.println("---------ajouter un etudiant-------------");

        String valeur;
        System.out.print("Entrez le nom de l'étudiant: ");
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

        String gensel = BCrypt.gensalt();
        ps2.setString(4, BCrypt.hashpw(valeur, gensel));

        try {
            ps2.executeQuery();
            System.out.println("--------> Insertion de l'étudiant REUSSI ! <------------");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de l'insertion de l'étudiant ! <------------");
            System.out.println();
        }

    }

    public void inscrireEtudiantUnCours() throws SQLException {
        System.out.println("---------Inscrire l'étudiant à un cours-------------");

        System.out.print("Entrez le mail de l'étudiant: ");
        String valeur = scanner.nextLine();
        ps3.setString(1, valeur);
        System.out.print("Entrez le code du cours(BINV****): ");
        valeur = scanner.nextLine();
        ps3.setString(2, valeur);

        try {
            ps3.executeQuery();
            System.out.println("--------> Inscription de l'étudiant a un cours est REUSSI ! <------------");
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de l'inscription de l'étudiant ! <------------");
            System.out.println();
        }

    }

    public void creerUnProjetPourUnCours() throws SQLException {
        System.out.println("---------Créer un projet pour un cours-------------");

        String valeur;
        System.out.print("Entrez l'identifiant du projet: ");
        valeur = scanner.nextLine();
        ps4.setString(1, valeur);
        System.out.print("Entrez le nom du projet: ");
        valeur = scanner.nextLine();
        ps4.setString(2, valeur);
        System.out.print("Entrez la date de début du projet (AAAA-MM-JJ): ");
        valeur = scanner.nextLine();
        ps4.setDate(3, java.sql.Date.valueOf(valeur));
        System.out.print("Entrez la date de fin du projet (AAAA-MM-JJ): ");
        valeur = scanner.nextLine();
        ps4.setDate(4, java.sql.Date.valueOf(valeur));
        System.out.print("Entrez le code du cours (BINV****): ");
        valeur = scanner.nextLine();
        ps4.setString(5, valeur);

        try {
            ps4.executeQuery();
            System.out.println("--------> Création du projet REUSSI ! <------------");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de la création du projet ! <------------");
            System.out.println();
        }

    }

    public void creerGroupePourUnProjet() throws SQLException {
        System.out.println("---------Créer des groupes pour un projet-------------");
        String valeur;
        int val;
        System.out.print("Entrez l'identifiant du projet: ");
        valeur = scanner.nextLine();
        ps5.setString(1, valeur);
        System.out.print("Combien de groupe: ");
        val = Integer.parseInt(scanner.nextLine());
        ps5.setInt(2, val);
        System.out.print("Entrez la taille du groupe: ");
        val = Integer.parseInt(scanner.nextLine());
        ps5.setInt(3, val);
        try {
            ps5.executeQuery();
            System.out.println("--------> Création du groupe est REUSSI ! <------------");
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de la création du groupe ! <------------");
            System.out.println();
        }
    }

    public void afficherCours() throws SQLException {
        System.out.println("---------------Afficher les cours-------------------------");

        ResultSet rs = ps6.executeQuery();
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

    public void afficherProjets() throws SQLException {
        System.out.println("---------Afficher les projets-------------");

        ResultSet rs = ps7.executeQuery();
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

    public void afficherCompositionsGroupePourUnProjet() throws SQLException {
        System.out.println("---------Afficher composition de groupe d'un projet-------------");

        System.out.print("Entrez l'identifiant de projet: ");
        String identifiantProjet = scanner.nextLine();

        ResultSet rs = ps8.executeQuery();
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
            System.out.print(resultSetMetaData.getColumnName(i) + "         ");
        }
        System.out.println();

        ps11.setString(1, identifiantProjet);
        ResultSet rs1;
        int idProjet = 0;
        try {
            rs1 = ps11.executeQuery();
            rs1.next();
            idProjet = rs1.getInt(1);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
        }
        if (idProjet == 0) {
            System.out.println("Projet inexistant");
            return;
        }
        while (rs.next()) {
            if (idProjet == rs.getInt(1)) {
                System.out.println(rs.getInt(2) + "         " + rs.getString(3)
                        + "         " + rs.getString(4)
                        + "         " + rs.getBoolean(5) + "         " + rs.getBoolean(6));
            }
        }
    }

    public void validerUnGroupe() throws SQLException {
        System.out.println("---------Valider un groupe d'un projet -------------");

        System.out.print("Entrez le numéro du groupe: ");
        int idGroupe = Integer.parseInt(scanner.nextLine());
        System.out.print("Entrez l'identifiant du projet: ");
        String idProjet = scanner.nextLine();

        ps9.setString(1, idProjet);
        ps9.setInt(2, idGroupe);

        try {
            ps9.executeQuery();
            System.out.println("--------> Validation du groupe REUSSI ! <------------");
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de la validation du groupe ! <------------");
            System.out.println();
        }

    }

    public void validerTousLesGroupesDUnProjet() throws SQLException {
        System.out.println("---------Valider tous les groupes d'un projet -------------");

        System.out.print("Entrez l'identifiant du projet: ");
        String idProjet = scanner.nextLine();

        ps10.setString(1, idProjet);
        try {
            ps10.executeQuery();
            System.out.println("--------> Validation des groupes REUSSI ! <------------");
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de la validation des groupes ! <------------");
            System.out.println();
        }

    }

    public void requeteDemo() {
        System.out.println("-----------------Scénario de démo-----------------------");
        try {
            psDemo = conn.prepareStatement("SELECT logiciel.inserer_cours('BINV2040', 'BD2', 2, 6)");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inserer_cours('BINV1020', 'APOO', 1, 6)");
            psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.inserer_projets('projSQL', 'projet SQL', '2023-09-10', '2023-12-15', 'BINV2040')");
//            psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.inserer_projets('dsd', 'DSD', '2023-09-30', '2023-12-01', 'BINV1020')");
//            psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.creer_groupes('projSQL', 1, 2)");
 //           psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(1, 1, 'projSQL')");
//            psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(2, 1, 'projSQL')");
//            psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.inserer_cours('BINV2140', 'SD2', 2, 3)");
//            psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours('ic@student.vinci.be', 'BINV2140')");
//            psDemo.executeQuery();
//        ;
//
//        -- SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be', 'BINV2040');
//        -- SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be', 'BINV2040');
//        --SELECT logiciel.inserer_etudiant('Cambron', 'Isabelle', 'ic@student.vinci.be','$2a$10$POaQZNkxmAhVNJG2TWvUF.vqN3tV3L2WiS2TTE7DZgMh9OY6DvNcG');
//
//        ne doit pas fonctionner le suivant:
//        --SELECT logiciel.inscrire_etudiant_cours('ic@student.vinci.be', 'BINV2040');
            //       --------------
//        --SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be', 'BINV2140');
//        --SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be', 'BINV2140');
//        --
//        --select logiciel.inserer_projets('projSD', 'projet SD2', '2023-03-01', '2023-04-01', 'BINV2140');
//        -- --ne doit pas fonctionner le suivant:
//        -- --SELECT logiciel.creer_groupes('projSD', 2, 2);
//        --
//                --SELECT logiciel.creer_groupes('projSD', 3, 1);
//        --
//                -- --ne doit pas fonctionner le suivant:
//        -- --SELECT logiciel.creer_groupes('projSD', 3, 1);
//        --
//                --SELECT logiciel.creer_groupes('projSD', 1, 2);
//
//        --ne doit pas fonctionner le suivant:
//        --SELECT logiciel.creer_groupes('Javascript', 2, 2);
//
//
//        -- SELECT logiciel.inscrire_etudiant_groupe(1, 1, 1);
//        -- SELECT logiciel.valider_un_groupe(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
