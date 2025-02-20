package Calculator;
import java.sql.*;
import JDBC.JDBC_connection;
import Calculator.*;

import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.time.*;

/* main file where all executions happens */
public class Fare_calculator {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("a");
        String s = df.format(dt);
        if(s.equals("pm")){
            System.out.println("Good evening!");
        } else{
            System.out.println("Good morning!");
        }
        DateTimeFormatter d = DateTimeFormatter.ofPattern("hh:mm a");
        String time = d.format(dt);
        System.out.println(time);
        System.out.println("................................WELCOME TO TRIP MATE.................................");

        /* infinite loop to repeatedly ask user for login and operations*/
        Features features = new Features();
        boolean found1 = true;
        while(found1){
                System.out.println("1. Login\n2. New user\n3. Delete account\n4. exit");
                String choice = sc.nextLine().trim().toLowerCase();
                Login user = new Login();
                switch(choice){
                    case "1":
                    case "login":
                        System.out.println("Enter username");
                        String username  = sc.nextLine().trim();
                        System.out.println("Enter password");
                        String password = sc.nextLine().trim();
                        Connection database = user.login(username, password);
                        if(database!=null){
                            boolean found2 = true;
                            while(found2){
                                // features of fare calculator are executed here
                                System.out.println("1. Enter participants\n2. calculate fare\n3. Show participants\n4. update participant\n5. Delete participant\n6. Log out");
                                String input = sc.nextLine().trim().toLowerCase();
                                switch(input){
                                    case "1":
                                    case "enter":
                                    case "enter participants":
                                        features.participants(database);
                                        break;
                                    case "2":
                                    case "calculate":
                                    case "calculate fare":
                                        features.calculate_fare(database);
                                        break;
                                    case "3":
                                    case "show participants":
                                        features.show_participants(database);
                                        break;
                                    case "4":
                                    case "update":
                                    case "update participant":
                                        features.update_participant(database);
                                        break;
                                    case "5":
                                    case "delete":
                                    case "delete participants":
                                        features.delete_Participant(database);
                                        break;
                                    case "6":
                                    case "log out":
                                        features.log_out(database);
                                        found2 = false;
                                        break;
                                    default:
                                        System.out.println("Invalid option. Please try again.");
                                        break;
                                }

                            }
                        }
                        break;
                    case "2":
                    case "new user":
                        System.out.println("Enter new username");
                        String new_username  = sc.nextLine().trim();
                        System.out.println("Enter new password(should not contain spaces )");
                        String new_password = sc.nextLine().trim();
                        user.new_user(new_username,new_password);
                        break;
                    case "3":
                    case "delete account":
                        // get connection of login table where username and password are stored
                        JDBC_connection con = new JDBC_connection();
                        Connection login = con.get_connection("login");
                        if(login!=null) {
                            // check if entered username and password matches the existing username and passsword
                                System.out.println("Enter username");
                                String delete_username = sc.nextLine().trim();
                                System.out.println("Enter password");
                                String delete_password = sc.nextLine().trim();
                                String query = "select COUNT(*) from login where username = ? AND password = ? ";
                                try(PreparedStatement st = login.prepareStatement(query)){
                                    st.setString(1,delete_username);
                                    st.setString(2,delete_password);
                                    ResultSet rs = st.executeQuery();
                                    rs.next();
                                    int count = rs.getInt(1);
                                    if(count>0){
                                        String query1 = "drop database "+delete_username;
                                        String query2 = "delete from login where username = ? AND password = ?";
                                        try(Statement st1 = login.createStatement();

                                            // delete user's database
                                            PreparedStatement st2 = login.prepareStatement(query2)){
                                            st1.executeUpdate(query1);

                                            // delete user's login credentials'
                                            st2.setString(1,delete_username);
                                            st2.setString(2,delete_password);
                                            System.out.println("Account deleted successfully");
                                            st2.executeUpdate();
                                        } catch(SQLException e){
                                            System.out.println("Failed to delete account"+e.getMessage());
                                        }
                                    } else{
                                        System.out.println("Invalid username or password");
                                    }
                                }catch(SQLException e){
                                    System.out.println("Failed to delete account"+e.getMessage());
                                }
                        }
                        break;
                    case "4":
                    case "exit":
                        System.out.println("Thank you for choosing us!!");
                        found1 = false;
                        break;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
            sc.close();
    }
}
