package Calculator;
import java.sql.*;
import java.util.Scanner;
import JDBC.JDBC_connection;
public class Features {
    Scanner sc = new Scanner(System.in);
    public void participants(Connection con) {
        boolean found = true;
        while (found) {
            System.out.println("Enter participants and amount paid separated by a Space\n1. Press 1 to Stop entering");
            String input = sc.nextLine().trim();

            switch (input) {
                case "1":
                    found = false;
                    break;
                default:
                    String[] parts = input.split(" ");

                    // Ensure exactly 2 inputs: Name & Amount
                    if (parts.length != 2) {
                        System.out.println("Invalid input! Please enter in format: Name Amount");
                        System.out.println();
                        break;
                    }

                    try {
                        String name = parts[0];
                        int amount = Integer.parseInt(parts[1]);  // Now safe

                        String query = "INSERT INTO participants(participant_name, amount_paid) VALUES(?,?)";
                        try (PreparedStatement st = con.prepareStatement(query)) {
                            st.setString(1, name);
                            st.setInt(2, amount);
                            int count = st.executeUpdate();
                            if (count > 0) {
                                System.out.println("Participant added successfully!");
                                System.out.println();
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount! Please enter a valid number");
                        System.out.println();
                    } catch (SQLException e) {
                        System.out.println("Error, please insert names again: " + e.getMessage());
                        System.out.println();
                    }
            }
        }
    }

    public void calculate_fare(Connection con){
        String query = "\n" +
                "-- Step 1: Calculate Fair Share for Each Participant\n" +
                "WITH summary AS (\n" +
                "    SELECT \n" +
                "        CAST(SUM(amount_paid) / COUNT(*) AS DECIMAL(10,2)) AS fair_share\n" +
                "    FROM participants\n" +
                "),\n" +
                "-- Step 2: Assign Fair Share to Each Participant and Calculate Balance\n" +
                "fair_share AS (\n" +
                "    SELECT \n" +
                "        p.id,\n" +
                "        p.participant_name,\n" +
                "        p.amount_paid,\n" +
                "        s.fair_share,\n" +
                "        s.fair_share - p.amount_paid AS amount_to_pay  -- Corrected formula (ensuring debtors have positive values)\n" +
                "    FROM participants p\n" +
                "    JOIN summary s\n" +
                "),\n" +
                "-- Step 3: Identify Debtors (Who Owe Money)\n" +
                "debtors AS (\n" +
                "    SELECT \n" +
                "        id,\n" +
                "        participant_name,\n" +
                "        amount_to_pay AS amount_owed  -- Renaming amount_to_pay for clarity\n" +
                "    FROM fair_share \n" +
                "    WHERE amount_to_pay > 0  -- Positive values indicate those who need to pay\n" +
                "),\n" +
                "-- Step 4: Identify Creditors (Who Are Owed Money)\n" +
                "creditors AS (\n" +
                "    SELECT \n" +
                "        id,\n" +
                "        participant_name,\n" +
                "        -amount_to_pay AS amount_due  -- Convert negative amount_to_pay to positive amount_due\n" +
                "    FROM fair_share \n" +
                "    WHERE amount_to_pay < 0  -- Negative values indicate those who are owed money\n" +
                ")\n" +
                "-- Step 5: Match Debtors and Creditors to Generate Settlement Transactions\n" +
                "SELECT \n" +
                "    d.participant_name AS payer, \n" +
                "    c.participant_name AS receiver, \n" +
                "    LEAST(d.amount_owed, c.amount_due) AS amount_to_pay\n" +
                "FROM debtors d  \n" +
                "JOIN creditors c \n" +
                "ON d.amount_owed > 0 AND c.amount_due > 0\n" +
                "ORDER BY d.participant_name, c.participant_name;\n" +
                "\t\t\t     ";
        try(PreparedStatement st = con.prepareStatement(query);
            ResultSet rs = st.executeQuery()){
            System.out.println("---------------------------------------------------------");
                while(rs.next()){
                    System.out.printf("%s should pay %s: %.2f%n",rs.getString("payer"),rs.getString("receiver"),rs.getDouble("amount_to_pay"));
                }
            System.out.println("---------------------------------------------------------");
        }catch(SQLException e){
            System.out.println("Error calculating fare!"+e.getMessage());
            System.out.println();
        }
    }
    public void update_participant(Connection con){
        // updates based on user interests
        System.out.println("1. change participant name\n2. change amount_paid");
            String choice = sc.nextLine().trim().toLowerCase();
            switch(choice){
                case "1":
                    System.out.println("Enter Participant name and id to change separated by space");
                    System.out.println("HINT : go to 'show participants' and know ids of participants");
                    String[] input = sc.nextLine().trim().split(" ");
                    if (input.length < 2) {
                        System.out.println("Invalid input format. Please enter both name and ID.");
                        System.out.println();
                        return;
                    }
                    String name = input[0];
                    int id;
                    // checks if user enters wrong format

                    try {
                        id = Integer.parseInt(input[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format. Please enter a valid number.");
                        System.out.println();
                        return;
                    }
                    // to Check if user entered participant exists
                    if (!participantExists(con, name, id)) {
                        System.out.println("Participant with given name and ID does not exist.");
                        System.out.println();
                        return;
                    }
                    System.out.println("Enter new participant name");
                    String newName = sc.nextLine().trim();
                    String updatequery = "update participants set participant_name = ? where id = ?";
                    try(PreparedStatement st = con.prepareStatement(updatequery)){
                        st.setString(1,newName);
                        st.setInt(2,id);
                        st.executeUpdate();
                        System.out.println("Participant name updated successfully!");
                        System.out.println();
                    }catch(SQLException e){
                        System.out.println("Failed to update participant name : "+e.getMessage());
                        System.out.println();
                    }
                    break;
                // to change amount paid by knowing participant name
                case "2":
                    System.out.println("Enter Participant name and id to change amount paid separated by space");
                    System.out.println("HINT : go to 'show participants' and know ids of participants");
                    String[] input1 = sc.nextLine().trim().split(" ");

                    // checks if user enters only id or name
                    if (input1.length < 2) {
                        System.out.println("Invalid input format. Please enter both name and ID.");
                        System.out.println();
                        return;
                    }
                    String name1 = input1[0];
                    int id1;

                    // to check if user enters wrong format
                    try {
                        id1 = Integer.parseInt(input1[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format. Please enter a valid number.");
                        System.out.println();
                        return;
                    }

                    // Check if participant exists
                    if (!participantExists(con, name1, id1)) {
                        System.out.println("Participant with given name and ID does not exist.");
                        return;
                    }
                    System.out.println("Enter new amount paid");
                    int new_amount;

                    // to check if user enters wrong format
                    try {
                        new_amount = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount format. Please enter a valid number.");
                        System.out.println();
                        return;
                    }
                    String updatequery1 = "update participants set amount_paid = ? where id = ? AND participant_name = ?";
                    try(PreparedStatement st = con.prepareStatement(updatequery1)){
                        st.setInt(1,new_amount);
                        st.setInt(2,id1);
                        st.setString(3,name1);
                        st.executeUpdate();
                        System.out.println("Participant's amount paid updated successfully!");
                        System.out.println();
                    }catch(SQLException e){
                        System.out.println("Failed to update amount paid : "+e.getMessage());
                        System.out.println();
                    }
            }
    }
    public void show_participants(Connection con){
        // to show user all participants in database
        String query = "select * from participants";
        try(PreparedStatement st = con.prepareStatement(query);
            ResultSet rs = st.executeQuery()){
            // whole table will be stored in rs
            System.out.println("---------------------------------------------------------");
            System.out.println("id  |     participants     |      amount paid   ");
            System.out.println("---------------------------------------------------------");
            while(rs.next()){
                System.out.println(rs.getInt(1)+"         "+rs.getString(2)+"                 "+rs.getInt(3));
            }
            System.out.println("---------------------------------------------------------");
        }catch(SQLException e){
            System.out.println("Failed to show participants : "+e.getMessage());
            System.out.println();
        }
    }

    public void delete_Participant(Connection con){
        System.out.println("Enter Participant name and id to delete by space");
        System.out.println("HINT : go to 'show participants' and know ids of participants");
        String[] input = sc.nextLine().trim().split(" ");
        if (input.length < 2) {
            System.out.println("Invalid input format. Please enter both name and ID.");
            System.out.println();
            return;
        }
        String name = input[0];
        int id;
        // checks if user enters wrong format

        try {
            id = Integer.parseInt(input[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a valid number.");
            System.out.println();
            return;
        }
        // to Check if user entered participant exists
        if (!participantExists(con, name, id)) {
            System.out.println("Participant with given name and ID does not exist.");
            System.out.println();
            return;
        }
        String query = " delete from participants where id = ?";
        try(PreparedStatement st = con.prepareStatement(query)){
            st.setInt(1,id);
            st.executeUpdate();
            System.out.println("Participant deleted successfully!");
            System.out.println();
        } catch (SQLException e){
            System.out.println(" Failed to delete participant : "+e.getMessage());
            System.out.println();
        }
    }

    // private method to check if participant exists in the database
    private boolean participantExists(Connection con, String name, int id) {
        String query = "SELECT COUNT(*) FROM participants WHERE participant_name = ? AND id = ?";
        try (PreparedStatement st = con.prepareStatement(query)) {
            st.setString(1, name);
            st.setInt(2, id);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;  //  Returns true if participant exists, false otherwise
        } catch (SQLException e) {
            System.out.println("Error checking participant existence: " + e.getMessage());
            System.out.println();
            return false;  // Returns false if an error occurs
        }
    }


    public void log_out(Connection con){
        // chech if connetion is null
        if(con==null){
            System.out.println("No active connection found!");
            System.out.println();
            return;
        }
        try{
            con.close();
            System.out.println("Logged out successfully!");
            System.out.println();
        } catch(SQLException e){
            System.out.println("Error logging out!!"+e.getMessage());
            System.out.println();
        }
    }
}
