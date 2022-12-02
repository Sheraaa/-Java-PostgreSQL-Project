import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class test {

    public static void main(String[] args) {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }


        String url="jdbc:postgresql://localhost:5432/postgres";


        Connection conn = null;


        try {
            conn = DriverManager.getConnection(url,"postgres","Mariam-16");
        } catch (SQLException e) {
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }

        /*
        Statement = instruction(s) à envoyer à la BD

                try {
                    Statement s = conn.createStatement();
                    s.executeUpdate("INSERT INTO exercice.utilisateurs "+
                            "VALUES (DEFAULT, ‘Damas', 'Christophe');");
                } catch (SQLException se) {
                    System.out.println("Erreur lors de l’insertion !");
                    se.printStackTrace();
                    System.exit(1);
                }


        ResultSet = Résultat d’un SELECT

                try {
                    Statement s = conn.createStatement();
                    try(ResultSet rs= s.executeQuery("SELECT nom"+                              ----> Ceci n'est pas un try...catch...finally !
                                                     "FROM exercice.utilisateurs;"){                  C'est un try avec ressource
                        while(rs.next()) {                                                            => la ressource sera fermée à la fin du bloc
                            System.out.println(rs.getString(1));                                      => équivalent à un finally {rs.close();}
                        }
                    }
                } catch (SQLException se) {
                    se.printStackTrace();
                    System.exit(1);
                }








        */
    }


}
