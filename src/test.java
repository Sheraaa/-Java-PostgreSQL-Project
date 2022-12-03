import java.sql.*;

public class test {

    public static void main(String[] args) {

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

        try {
            Statement s = conn.createStatement();
            s.executeUpdate("INSERT INTO logiciel.etudiants(nom,prenom,mail) " +
                    "VALUES ('CO', 'co','co12@student.vinci.be');");
        } catch (SQLException se) {
            System.out.println("Erreur lors de l’insertion !");
            se.printStackTrace();
            System.exit(1);
        }
//        try {
//            Statement s = conn.createStatement();
//            try (ResultSet rs = s.executeQuery("SELECT nom" +
//                    "FROM exercice.utilisateurs;")){
//                while (rs.next()) {
//                    System.out.println(rs.getString(1));
//                }
//            }
//        } catch (SQLException se) {
//            se.printStackTrace();
//            System.exit(1);
//        }
    }
}
