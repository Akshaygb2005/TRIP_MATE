package Calculator;
import java.sql.*;
import JDBC.JDBC_connection;

public class Login {
/* to create separate table to store login credentials*/
public void create_table() {
    JDBC_connection connect = new JDBC_connection(); // Assuming you have a JDBC_connection class
    Connection con = connect.get_connection("login"); // Get database connection

    if (con != null) {
        try {
            String query = "CREATE TABLE IF NOT EXISTS login (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " + // Added PRIMARY KEY for id
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) UNIQUE NOT NULL" +  // Removed the trailing comma
                    ")";

            PreparedStatement st = con.prepareStatement(query);
            st.executeUpdate();
            System.out.println("✅ Table created successfully or already exists.");
            st.close(); // Close statement
        } catch (SQLException e) {
            System.out.println("❌ Error creating table");
            System.out.println("Error: " + e.getMessage());
        }
    } else {
        System.out.println("❌ Database connection failed!");
    }
}
    /* to validate already existing users and connect to username's database */
    public Connection login(String username, String password) {
        JDBC_connection connect = new JDBC_connection(); // Assuming you have a JDBC_connection class
        Connection con = connect.get_connection("login");
        Connection user_database = null;
        if (con!=null){
            try{
                String query = "select COUNT(*) from login WHERE BINARY username = ? AND BINARY password = ?";
                PreparedStatement st = con.prepareStatement(query);
                st.setString(1,username);
                st.setString(2,password);
                ResultSet rs = st.executeQuery();// whole table will be stored in rs
                rs.next();
                int count = rs.getInt(1);

                //gives count. if count > 0 then username exists else it doesnt
                if(count>0){
                    /* if login is successful this will connect to user's database*/
                    user_database = connect.get_connection(username);
//                    String query1 = "use "+username;
//                    PreparedStatement st1 = con.prepareStatement(query1);
//                    st1.executeUpdate();
                    System.out.println("Login successful!");
                } else {
                    System.out.println("Invalid username or password!");
                }
                rs.close();
                st.close();
                con.close();

            } catch(SQLException e){
                System.out.println("Error using database");
                System.out.println("Error: " + e.getMessage());
            }
        }
        return user_database;
    }
    public void new_user(String username, String password){
        /* to sign up new users. It also checks whether new username exists or not if ti exists it give gives message
        * and user should again login after signing up*/
        JDBC_connection connect = new JDBC_connection();
        Connection con = connect.get_connection("login");
        if(con!=null){
            try{
                String query = "select COUNT(*) from login WHERE username = ? ";
                PreparedStatement st = con.prepareStatement(query);
                st.setString(1,username);
                ResultSet rs = st.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                //if count > 0 then username exists then we have more than 1 user with same username
                if(count>0){
                    System.out.println("username already exists!!..........");
                } else {
                    try{
                        //this will insert username and password to table
                        String query1 = "insert into login(username,password) values (?,?)";
                        PreparedStatement st1 = con.prepareStatement(query1);
                        st1.setString(1,username);
                        st1.setString(2,password);
                        int status = st1.executeUpdate();
                        //if insertion is successful then create new database
                        if (status > 0) {
                            String query2 = "create database "+username;
                            PreparedStatement st2 = con.prepareStatement(query2);
                            st2.executeUpdate();

                            // **Create a new connection to the user's database**
                            Connection userCon = connect.get_connection(username);
                            if (userCon != null) {
                                // **Create the 'participants' table inside the username's database**
                                String query3 = "CREATE TABLE IF NOT EXISTS participants (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                        "participant_name VARCHAR(50) UNIQUE NOT NULL, " +
                                        "amount_paid DECIMAL(10,2) NOT NULL" +  // Using DECIMAL for accurate currency storage
                                        ")";
                                PreparedStatement st3 = userCon.prepareStatement(query3);
                                st3.executeUpdate();
                                st3.close();
                                userCon.close();
                            }
                            System.out.println("New account created successfully.........");
                        }
                    }catch(SQLException e ){
                        System.out.println("Failed to create new user");
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                st.close();
                con.close();

            } catch(SQLException e){
                System.out.println("Error using database");
                System.out.println("Error: " + e.getMessage());
            }
        }
      }
    }

