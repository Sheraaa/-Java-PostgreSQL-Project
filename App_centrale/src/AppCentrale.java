import java.sql.*;
import java.util.Scanner;

public class AppCentrale {
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
   // private final String url = "jdbc:postgresql://localhost:5432/logiciel";
    // private final String url = "jdbc:postgresql://localhost:5432/postgres";
  //  private static String url="jdbc:postgresql://172.24.2.6/dbchehrazadouazzani";
    private static String url="jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani";
    private static Scanner scanner = new Scanner(System.in);

    /**
     * connect to postgresql and prepare the statements
     */
    public AppCentrale() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }

        try {
            conn=DriverManager.getConnection(url,"chehrazadouazzani","SQINPAG0B");
         //   conn = DriverManager.getConnection(url, "postgres", "shera");
        } catch (SQLException e) {
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }
        prepareStatements();
    }

    public void menu() {
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

    /**
     * prepare all the statements
     */
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

    /**
     * create a course
     * ps1 = SELECT logiciel.inserer_cours(?,?,?,?)
     */
    public void ajouterUnCours() {
        System.out.println("---------ajouter un cours-------------");

        System.out.print("Entrez le code du cours(BINV****): ");
        String valeur = scanner.nextLine();
        try {
            ps1.setString(1, valeur);

            System.out.print("Entrez le nom du cours: ");
            valeur = scanner.nextLine();
            ps1.setString(2, valeur);

            System.out.print("Entrez le bloc: ");
            int nbValeur = Integer.parseInt(scanner.nextLine());
            ps1.setInt(3, nbValeur);

            System.out.print("Entrez le nombre de crédits: ");
            nbValeur = Integer.parseInt(scanner.nextLine());
            ps1.setInt(4, nbValeur);

            ps1.executeQuery();
            System.out.println("--------> Insertion du cours REUSSI ! <------------");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de l'insertion  du cours! <------------");
            System.out.println();
        }
    }

    /**
     * create a student
     * ps2 = SELECT logiciel.inserer_etudiant(?,?,?,?)
     */
    public void ajouterUnEtudiant() {
        System.out.println("---------ajouter un etudiant-------------");

        System.out.print("Entrez le nom de l'étudiant: ");
        String valeur = scanner.nextLine();
        try {
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
            ps2.executeQuery();
            System.out.println("--------> Insertion de l'étudiant REUSSI ! <------------");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de l'insertion de l'étudiant ! <------------");
            System.out.println();
        }

    }

    /**
     * subscribe a student into a course
     * ps3 = SELECT logiciel.inscrire_etudiant_cours(?,?)
     */
    public void inscrireEtudiantUnCours() {
        System.out.println("---------Inscrire l'étudiant à un cours-------------");

        System.out.print("Entrez le mail de l'étudiant: ");
        String valeur = scanner.nextLine();
        try {
            ps3.setString(1, valeur);
            System.out.print("Entrez le code du cours(BINV****): ");
            valeur = scanner.nextLine();
            ps3.setString(2, valeur);
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

    /**
     * create a project
     * ps4 =SELECT logiciel.inserer_projets(?,?,?,?,?)
     */
    public void creerUnProjetPourUnCours() {
        System.out.println("---------Créer un projet pour un cours-------------");

        System.out.print("Entrez l'identifiant du projet: ");
        try {
            String valeur = scanner.nextLine();
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

            ps4.executeQuery();
            System.out.println("--------> Création du projet REUSSI ! <------------");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de la création du projet ! <------------");
            System.out.println();
        }

    }

    /**
     * create groups for a project
     * ps5 = SELECT logiciel.creer_groupes(?,?,?)
     * ps11 = SELECT logiciel.chercher_id_projet(?)
     */
    public void creerGroupePourUnProjet() {
        System.out.println("---------Créer des groupes pour un projet-------------");

        System.out.print("Entrez l'identifiant du projet: ");
        String valeur = scanner.nextLine();
        try {
            ps11.setString(1, valeur);
            ResultSet rs = ps11.executeQuery();
            rs.next();
            ps5.setInt(1, rs.getInt(1));
            System.out.print("Combien de groupe: ");
            int val = Integer.parseInt(scanner.nextLine());
            ps5.setInt(2, val);
            System.out.print("Entrez la taille du groupe: ");
            val = Integer.parseInt(scanner.nextLine());
            ps5.setInt(3, val);
            ps5.executeQuery();
            System.out.println();
            System.out.println("--------> Création du groupe est REUSSI ! <------------");
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de la création du groupe ! <------------");
            System.out.println();
        }
    }

    /**
     * display all the courses
     * ps6 = SELECT * FROM logiciel.afficher_cours
     */
    public void afficherCours() {
        System.out.println("---------------Afficher les cours-------------------------");
        try {
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("----------------------------------------------------------");
    }

    /**
     * display all the projects
     * ps7 = SELECT * FROM logiciel.afficher_projets
     */
    public void afficherProjets() {
        System.out.println("-------------------------------Afficher les projets----------------------------------------------------------------------------------------------------------");
        try {
            ResultSet rs = ps7.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();

            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "\t\t\t\t");
            }
            System.out.println();
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getString(1) + "\t\t\t\t" + rs.getString(2) +
                        "\t\t\t\t" + rs.getString(3) + "\t\t\t\t\t\t" + rs.getString(4)
                        + "\t\t\t\t\t\t\t" + rs.getString(5) + "\t\t\t\t\t\t\t\t" + rs.getString(6));
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * display all the groups of one specific project
     * ps8 = SELECT * FROM logiciel.afficher_composition_groupe
     * ps11 = SELECT logiciel.chercher_id_projet(?)
     */
    public void afficherCompositionsGroupePourUnProjet() {
        System.out.println("-----------------------Afficher composition de groupe d'un projet------------------------");

        System.out.print("Entrez l'identifiant de projet: ");
        String identifiantProjet = scanner.nextLine();

        try {
            ResultSet rs = ps8.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "         ");
            }
            ps11.setString(1, identifiantProjet);
            ResultSet rs1 = ps11.executeQuery();
            rs1.next();

            int idProjet = rs1.getInt(1);

            System.out.println();
            System.out.println("---------------------------------------------------------------------------------------------");

            while (rs.next()) {
                if (idProjet == rs.getInt(1)) {
                    System.out.println(rs.getInt(2) + "            " + rs.getString(3)
                            + "            " + rs.getString(4)
                            + "            " + rs.getBoolean(5) + "            " + rs.getBoolean(6));
                }
            }
            System.out.println("---------------------------------------------------------------------------------------------");
            System.out.println();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * validate a group
     * ps9 = SELECT logiciel.valider_un_groupe(?,?)
     * ps11 = SELECT logiciel.chercher_id_projet(?)
     */
    public void validerUnGroupe() {
        System.out.println("---------Valider un groupe d'un projet -------------");

        System.out.print("Entrez l'identifiant du projet: ");
        String identifiantProjet = scanner.nextLine();
        System.out.print("Entrez le numéro du groupe: ");
        int idGroupe = Integer.parseInt(scanner.nextLine());

        try {
            ps11.setString(1, identifiantProjet);
            ResultSet rs1 = ps11.executeQuery();
            rs1.next();

            int idProjet = rs1.getInt(1);

            ps9.setInt(1, idProjet);
            ps9.setInt(2, idGroupe);
            ResultSet rs = ps9.executeQuery();
            rs.next();
            if (rs.getBoolean(1)) {
                System.out.println("--------> Validation du groupe REUSSI ! <------------");
                System.out.println();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println("--------> ECHEC de la validation du groupe ! <------------");
            System.out.println();
        }
    }

    /**
     * validate all the groups of a project
     * ps10 = SELECT logiciel.valider_tous_les_groupes(?)
     */
    public void validerTousLesGroupesDUnProjet() {
        System.out.println("---------Valider tous les groupes d'un projet -------------");

        System.out.print("Entrez l'identifiant du projet: ");
        String idProjet = scanner.nextLine();

        try {
            ps10.setString(1, idProjet);
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
}
