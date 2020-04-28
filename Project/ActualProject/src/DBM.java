import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

public class DBM {
    Connection con;
    String userID;
    String access;
    Timestamp loginTime;
    Timestamp logoutTime;

    public DBM(Connection connect, String username, String accesstype){
        con = connect;
        userID = username;
        access = accesstype;

        //get current time for the login table
        Date currentTime = new Date();
        long time = currentTime.getTime();
        loginTime = new Timestamp(time);
    }

    public void exit(){
        //get current time of logout
        Date currentTime = new Date();
        long time = currentTime.getTime();
        logoutTime = new Timestamp(time);

        //insert info into login table
        try {
            String insertLogin = "insert into login(userid,privilege,logintime,logouttime) values (?,?,?,?)";
            PreparedStatement prepstmt = con.prepareStatement(insertLogin);
            prepstmt.setString(1, userID);
            prepstmt.setString(2, access);
            prepstmt.setTimestamp(3,loginTime);
            prepstmt.setTimestamp(4, logoutTime);
            prepstmt.executeUpdate();
            prepstmt.close();
            con.commit();
        }catch(Exception ie){
            ie.printStackTrace();
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        }
    }

    public void create() {//create table
        if(!access.equalsIgnoreCase("Admin")){//not admin, cant create table
            System.out.println("No access");
            return;
        }
        Scanner in = new Scanner(System.in);
        System.out.println("What's the name of the table you want to create?");
        String name = in.nextLine();
        System.out.println( "Type the columns of the table separated by a comma" + "\n" +
                            "In this format - columnName type1, columnName2 type2" + "\n" +
                            "Example - userID varchar(255), number int");
        String columns = in.nextLine();
        System.out.println( "Type the constraints/primary/foreign key separated by commas" + "\n" +
                            "Example - primary key (userID), check(userID > 0)");
        String constraints = in.nextLine();
        try{
            Statement stmt = con.createStatement();
            String createTable = "create table " + name + "(" + columns + ", " + constraints + ")";
            stmt.executeUpdate(createTable);
            stmt.close();
            con.commit();
            System.out.println("Table created");
        }catch(Exception ie){
            ie.printStackTrace();
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        }
    }

    public void view() {
        //sales can view customer, orders, ordermodels, Inventory
        //engineers can view model, inventory, and partial employee - made a view specifically for it, hmm how to access that
        //hr can view employee and orders - project description says can view employee and associated sales number hmmm
        try {
            Statement statement = con.createStatement();
            String str = ask();
            String sql = String.format("select * from %s", str); //use a format to make user input becomes a sql statement
            ResultSet resultSet = statement.executeQuery(sql);
            System.out.println(str);

            if (access.equalsIgnoreCase("Sales")) {//Sales tables
                //System.out.println("You can view customer, orders, ordermodels, inventory.");
                if (str.equalsIgnoreCase("customer")||str.equalsIgnoreCase("orders")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.println();
                    }
                }
                else if(str.equalsIgnoreCase("ordermodels")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.println();
                    }
                }
                else if(str.equalsIgnoreCase("inventory")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.print(resultSet.getString(5) + "\t");
                        System.out.println();
                    }
                }
                else {
                    System.out.println("Sorry. You cannot see it.");
                }
            }

            else if (access.equalsIgnoreCase("Engineering")) {//Engineer tables, I only let engineer to see lname, fname, employeeid and job type.
                //System.out.println("You can view model, inventory, and partial employee.");//Not sure what needs to be limited.
                if(str.equalsIgnoreCase("model")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.println();
                    }
                }
                else if(str.equalsIgnoreCase("inventory")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.print(resultSet.getString(5) + "\t");
                        System.out.println();
                    }
                }
                else if(str.equalsIgnoreCase("employee")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.print(resultSet.getString(8) + "\t");
                        System.out.println();
                    }
                }
                else {
                    System.out.println("Sorry. You cannot see it.");
                }
            }

            else if (access.equalsIgnoreCase("HR")) {//HR tables
                //System.out.println("You can view employee and orders.");
                if(str.equalsIgnoreCase("employee")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.print(resultSet.getString(5) + "\t");
                        System.out.print(resultSet.getString(6) + "\t");
                        System.out.print(resultSet.getString(7) + "\t");
                        System.out.print(resultSet.getString(8) + "\t");
                        System.out.println();
                    }
                }
                else if (str.equalsIgnoreCase("orders")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.println();
                    }
                }
                else {
                    System.out.println("Sorry. You cannot see it.");
                }
            }
            else if (access.equalsIgnoreCase("Admin")) {//Admin tables
                //System.out.println("You can view all.");
                if (str.equalsIgnoreCase("customer")||str.equalsIgnoreCase("orders")||str.equalsIgnoreCase("login")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.println();
                    }
                }
                else if(str.equalsIgnoreCase("ordermodels")||str.equalsIgnoreCase("model")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.println();
                    }
                }
                else if(str.equalsIgnoreCase("employee")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.print(resultSet.getString(5) + "\t");
                        System.out.print(resultSet.getString(6) + "\t");
                        System.out.print(resultSet.getString(7) + "\t");
                        System.out.print(resultSet.getString(8) + "\t");
                        System.out.println();
                    }
                }
                else if(str.equalsIgnoreCase("inventory")) {
                    while (resultSet.next()){
                        System.out.print(resultSet.getString(1) + "\t");
                        System.out.print(resultSet.getString(2) + "\t");
                        System.out.print(resultSet.getString(3) + "\t");
                        System.out.print(resultSet.getString(4) + "\t");
                        System.out.print(resultSet.getString(5) + "\t");
                        System.out.println();
                    }
                }
                else {
                    System.out.println("Sorry. You cannot see it.");
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            //System.out.println("Connect fail.");
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        }
    }

    public void insert() {
        //admin can insert anywhere
        //sale can insert on customer, order, ordermodels
        //engineer can insert on model, inventory
        //hr cant insert - can only view and update employee
        Scanner in = new Scanner(System.in);
        System.out.println("What table would you like to insert to?");
        String table = in.nextLine();
        //check if they can insert or not
        //if cant, break out of method
        if(access.equalsIgnoreCase("Sales") && (!table.equalsIgnoreCase("Customer") && !table.equalsIgnoreCase("Orders") && !table.equalsIgnoreCase("OrderModels"))){
            System.out.println("No Access");
            return;
        }else if(access.equalsIgnoreCase("Engineering") && (!table.equalsIgnoreCase("Model") && !table.equalsIgnoreCase("Inventory"))){
            System.out.println("No Access");
            return;
        }else if(access.equalsIgnoreCase("HR")){
            System.out.println("No Access");
            return;
        }
        //get columns of table and ask user what they want to insert
        //try catch and do that
        //catch would be wrong input, prob doesnt satisfy constraints
        try {
            Statement stmt = con.createStatement();
            //get column names
            ResultSet rs = stmt.executeQuery("Select * from " + table);
            ResultSetMetaData rsMeta = rs.getMetaData();
            int numColumns = rsMeta.getColumnCount();
            String[] columnTypes = new String[numColumns];
            String[] columnNames = new String[numColumns];
            for(int i = 1; i < numColumns + 1; i++){
                String colName = rsMeta.getColumnName(i);
                String colType = rsMeta.getColumnClassName(i);
                columnNames[i - 1] = colName;
                columnTypes[i - 1] = colType;
            }
            //create initial insert statement
            String insertToTable = "insert into " + table + " values (";
            for(int i = 0; i < numColumns; i++){
                if(i == numColumns - 1)
                    insertToTable += "?)";
                else
                    insertToTable += "?,";
            }
            PreparedStatement prepstmt = con.prepareStatement(insertToTable);
            //vars if inserting into employee
            String userID = null;
            String access = null;
            //vars if inserting order
            String orderNumber = null;
            //get parameters
            System.out.println("Insert values for the table " + table + ": ");
            for(int i = 1; i < columnNames.length + 1; i++){
                System.out.print(columnNames[i - 1] + " - " + columnTypes[i - 1] + ": ");
                String input = in.nextLine();
                //check columntype and make it how its supposed to be
                if(columnTypes[i - 1].equals("java.math.BigDecimal")){
                    BigDecimal temp = new BigDecimal(input);
                    prepstmt.setBigDecimal(i, temp);
                }else if(columnTypes[i - 1].equals("java.sql.Time")){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                    Time time = new Time(dateFormat.parse(input).getTime());
                    prepstmt.setTime(i, time);
                }else if(columnTypes[i - 1].equals("java.sql.Timestamp")){
                    try{
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date parsedDate = dateFormat.parse(input);
                        Timestamp time = new Timestamp(parsedDate.getTime());
                        prepstmt.setTimestamp(i, time);
                    }catch(Exception ie){
                        ie.printStackTrace();
                        System.out.println("Invalid - Shutting down");
                        APP.forceExit();
                    }
                }else if(columnTypes[i - 1].equals("java.lang.String")){
                    prepstmt.setString(i, input);
                    if(table.equalsIgnoreCase("Employee")) {//if adding in to employee
                        if (i - 1 == 4) {
                            userID = input;
                        }
                        else if (i - 1 == 7) {
                            access = input;
                        }
                    }
                    if(table.equalsIgnoreCase("Orders")){
                        if(i - 1 == 0)
                            orderNumber = input;
                    }
                }else if(columnTypes[i - 1].equals("java.lang.Integer")){
                    int temp = Integer.parseInt(input);
                    prepstmt.setInt(i, temp);
                }
            }
            //if creating an order, first ask for the models bought and cross reference if in inventory and add
            //to ordermodels table and update the invenotry table

            //orderNumber is the var containing orderID
            Vector<PreparedStatement> updates = new Vector<>();
            if(table.equalsIgnoreCase("Orders")){
                //check inventory table
                //add models and amount into a dictionary/hashtable
                Hashtable<String, Integer> modelAmount = new Hashtable<>();
                try{
                    ResultSet om = stmt.executeQuery("Select * from Inventory");
                    while(om.next()) {
                        //read values in table
                        String modelNumber = om.getString("model_number");
                        int amt = om.getInt("amount");
                        //insert into hashtable
                        modelAmount.put(modelNumber, amt);
                    }
                }catch(Exception ie){
                    ie.printStackTrace();
                    System.out.println("Invalid - Shutting down");
                    APP.forceExit();
                }
                System.out.println("Input the models bought in the order");
                boolean run = true;
                while(run){
                    System.out.print("Model: ");
                    String model = in.nextLine();
                    System.out.print("Amount: ");
                    int amount = in.nextInt();
                    in.nextLine();//dump
                    //check if in inventory
                    if(!modelAmount.containsKey(model) || amount > modelAmount.get(model)) {
                        System.out.println("Invalid amount or model, Reinsert again");
                        return;
                    }
                    //if it is, update the value
                    int amountLeft = modelAmount.get(model) - amount;
                    String update = "update Inventory set amount = ? where model_number = ?";
                    PreparedStatement ps1 = con.prepareStatement(update);
                    ps1.setInt(1, amountLeft);
                    ps1.setString(2, model);
                    updates.add(ps1);
                    //after checks all these, add to ordermodels table
                    String add = "insert into OrderModels values (?,?,?)";
                    PreparedStatement ps = con.prepareStatement(add);
                    ps.setString(1, orderNumber);
                    ps.setString(2, model);
                    ps.setInt(3, amount);
                    updates.add(ps);

                    //ask user if more to add
                    System.out.println("More to add? If so, type yes or no");
                    String input = in.nextLine();
                    if(input.equalsIgnoreCase("no")){
                        run = false;
                        break;
                    }
                }
            }
            //ask user for password if adding into employee
            if(table.equalsIgnoreCase("Employee")){
                System.out.println("Adding an employee, need to add login information as well");
                String file = "C:/Users/mlgam/OneDrive/Desktop/CS Projects/CS-425/Project/ActualProject/src/logins.csv";
                try{
                    createUser(file, userID, null, access);
                }catch(Exception ie){
                    System.out.println("Error, unable to insert");
                    System.out.println("Invalid - Shutting down");
                    APP.forceExit();
                }
            }
            //statement finished for inserting into table
            prepstmt.executeUpdate();
            prepstmt.close();
            con.commit();
            //if inserting into order, need to update inventory and ordermodels
            if(updates.size() != 0){
                for(PreparedStatement ps : updates){
                    ps.executeUpdate();
                    ps.close();
                    con.commit();
                }
            }
            System.out.println("Insert successful");

        }catch(Exception ie){
            ie.printStackTrace();
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        }
    }

    public void createUser(String filepath, String username, String password, String accessType){
        if(access.equalsIgnoreCase("Admin")) { // only admins can create a user
            Scanner in = new Scanner(System.in);
            System.out.println("Creating user ...");
            if(username == null) {
                System.out.print("Username: ");
                username = in.nextLine();
            }
            if(password == null) {
                System.out.print("Password: ");
                password = in.nextLine();
            }
            if(accessType == null) {
                System.out.print("Access: ");
                accessType = in.nextLine();
            }

            //write to a file
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(filepath, true));
                out.write(username + "," + password + "," + accessType + "\n");
                out.close();
                System.out.println("User created successfully");
            }catch(Exception ie){
                ie.printStackTrace();
                System.out.println("Invalid - Shutting down");
                APP.forceExit();
            }
        }else{
            System.out.println("No access");
        }
    }

    public void update() {
        //sales can update customer
        //engineers can update model, inventory
        //hr can update employee
        try {
            Scanner in = new Scanner(System.in);
            System.out.println("(U) Update or (D) drop a row");
            String choice1 = in.nextLine();
            if(choice1.equalsIgnoreCase("U")) {
                if (access.equalsIgnoreCase("Sales")) {
                    System.out.println("You can update customer table.");
                    String[] ans = ask1();
                    String sql = "update customer set " + ans[0] + " = ? where customer_id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, ans[1]);
                    ps.setString(2, ans[2]);
                    ps.executeUpdate();
                    ps.close();
                } else if (access.equalsIgnoreCase("Engineering")) {
                    System.out.println("You can update model table or inventory table.");
                    System.out.println("model table or inventory table?");
                    String choice = in.nextLine();
                    if (choice.toLowerCase().equals("model")) {
                        String[] ans = ask1();
                        String sql = "update model set " + ans[0] + " = ? where model_number = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("sale_price"))//if set sale_price = ?
                            ps.setInt(1, Integer.parseInt(ans[1]));
                        else//if set origin = ? or model_number
                            ps.setString(1, ans[1]);
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else if (choice.toLowerCase().equals("inventory")) {
                        String[] ans = ask1();
                        String sql = "update inventory set " + ans[0] + " = ? where model_number = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("costs") || ans[0].equalsIgnoreCase("amount"))//need to be int
                            ps.setInt(1, Integer.parseInt(ans[1]));
                        else if (ans[0].equalsIgnoreCase("lead_time")) {//need to be time
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                            Time time = new Time(dateFormat.parse(ans[1]).getTime());
                            ps.setTime(1, time);
                        } else {//updating category or model_number so just string
                            ps.setString(1, ans[1]);
                        }
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    }
                } else if (access.equalsIgnoreCase("HR")) {
                    System.out.println("You can update employee table.");
                    String[] ans = ask1();
                    String sql = "update employee set + " + ans[0] + " = ? where employee_id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    if (ans[0].equalsIgnoreCase("salary"))//salary needs to be int
                        ps.setInt(1, Integer.parseInt(ans[1]));
                    else//other columns are all string
                        ps.setString(1, ans[1]);
                    ps.setString(2, ans[2]);
                    ps.executeUpdate();
                    ps.close();
                } else if (access.equalsIgnoreCase("Admin")) {
                    //System.out.println("You can create a new employee in employee table.");
                    System.out.println("Which table?");
                    String choice = in.nextLine();
                    String[] ans = ask1();
                    if (choice.toLowerCase().equals("login")) {
                        String sql = "update login set " + ans[0] + " = ? where userID = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("userID") || ans[0].equalsIgnoreCase("privilege"))//just string
                            ps.setString(1, ans[1]);
                        else {//timestamp
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            Date parsedDate = dateFormat.parse(ans[1]);
                            Timestamp time = new Timestamp(parsedDate.getTime());
                            ps.setTimestamp(1, time);
                        }
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else if (choice.toLowerCase().equals("model")) {
                        String sql = "update model set " + ans[0] + " = ? where model_number = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("sale_price"))//if set sale_price = ?
                            ps.setInt(1, Integer.parseInt(ans[1]));
                        else//if set origin = ?
                            ps.setString(1, ans[1]);
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else if (choice.toLowerCase().equals("inventory")) {
                        String sql = "update inventory set " + ans[0] + " = ? where model_number = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("costs") || ans[0].equalsIgnoreCase("amount"))//need to be int
                            ps.setInt(1, Integer.parseInt(ans[1]));
                        else if (ans[0].equalsIgnoreCase("lead_time")) {//need to be time
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                            Time time = new Time(dateFormat.parse(ans[1]).getTime());
                            ps.setTime(1, time);
                        } else {//updating category or model_number so just string
                            ps.setString(1, ans[1]);
                        }
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else if (choice.toLowerCase().equals("orders")) {
                        String sql = "update orders set " + ans[0] + " = ? where order_number = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("sale_value"))//int
                            ps.setInt(1, Integer.parseInt(ans[1]));
                        else//string
                            ps.setString(1, ans[1]);
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else if (choice.toLowerCase().equals("ordermodels")) {
                        String sql = "update ordermodels set " + ans[0] + " = ? where order_number = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("amount"))//int
                            ps.setInt(1, Integer.parseInt(ans[1]));
                        else//string
                            ps.setString(1, ans[1]);
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else if (choice.toLowerCase().equals("employee")) {
                        String sql = "update employee set " + ans[0] + " = ? where employee_id = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        if (ans[0].equalsIgnoreCase("salary"))//salary needs to be int
                            ps.setInt(1, Integer.parseInt(ans[1]));
                        else//other columns are all string
                            ps.setString(1, ans[1]);
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else if (choice.toLowerCase().equals("customer")) {
                        String sql = "update customer set " + ans[0] + " = ? where customer_id = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, ans[1]);
                        ps.setString(2, ans[2]);
                        ps.executeUpdate();
                        ps.close();
                    } else {
                        System.out.println("Do not have that table.");
                    }
                }
                con.commit();
                System.out.println("Update successful");
            }else if(choice1.equalsIgnoreCase("D")){//drop row
                System.out.println("Which table?");
                String table = in.nextLine();
                //check access
                //sales can update customer
                //engineers can update model, inventory
                //hr can update employee
                if(access.equalsIgnoreCase("Sales") && !table.equalsIgnoreCase("Customer")){
                    System.out.println("No access");
                    return;
                }else if(access.equalsIgnoreCase("Engineering") && (!table.equalsIgnoreCase("Model") && !table.equalsIgnoreCase("Inventory"))){
                    System.out.println("No Access");
                    return;
                }else if(access.equalsIgnoreCase("HR") && !table.equalsIgnoreCase("Employee")){
                    System.out.println("No access");
                    return;
                }

                if(table.equalsIgnoreCase("Customer")){
                    System.out.print("Customer_ID: ");
                    String customer = in.nextLine();
                    String del = "delete from customer where customer_id = ?";
                    PreparedStatement ps = con.prepareStatement(del);
                    ps.setString(1, customer);
                    ps.executeUpdate();
                    ps.close();
                }else if(table.equalsIgnoreCase("Employee")){
                    System.out.print("employee_id: ");
                    String id = in.nextLine();
                    String del = "delete from employee where employee_id = ?";
                    PreparedStatement ps = con.prepareStatement(del);
                    ps.setString(1, id);
                    ps.executeUpdate();
                    ps.close();
                }else if(table.equalsIgnoreCase("Orders")){
                    System.out.print("order_number: ");
                    String order = in.nextLine();
                    String del = "delete from orders where order_number = ?";
                    PreparedStatement ps = con.prepareStatement(del);
                    ps.setString(1, order);
                    ps.executeUpdate();
                    ps.close();
                }else if(table.equalsIgnoreCase("Ordermodels")){
                    System.out.print("order_number: ");
                    String order = in.nextLine();
                    System.out.print("model: ");
                    String model = in.nextLine();
                    String del = "delete from ordermodels where order_number = ? and model = ?";
                    PreparedStatement ps = con.prepareStatement(del);
                    ps.setString(1, order);
                    ps.setString(2, model);
                    ps.executeUpdate();
                    ps.close();
                }else if(table.equalsIgnoreCase("Model")){
                    System.out.print("model_number: ");
                    String model = in.nextLine();
                    String del = "delete from model where model_number = ?";
                    PreparedStatement ps = con.prepareStatement(del);
                    ps.setString(1, model);
                    ps.executeUpdate();
                    ps.close();
                }else if(table.equalsIgnoreCase("Inventory")){
                    System.out.print("model_number: ");
                    String model = in.nextLine();
                    System.out.print("category: ");
                    String category = in.nextLine();
                    String del = "delete from inventory where model_number = ? and category = ?";
                    PreparedStatement ps = con.prepareStatement(del);
                    ps.setString(1, model);
                    ps.setString(2, category);
                    ps.executeUpdate();
                    ps.close();
                }else{
                    System.out.println("Invalid table");
                }
                System.out.println("Dropped successfully");
                con.commit();
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        }
    }

    public void grant() {
        if(!access.equalsIgnoreCase("Admin")){ //if not admin, then cant grant
            System.out.println("No access");
            return;
        }
        Scanner in = new Scanner(System.in);
        System.out.println("Change access type of a user, or grant access? C or G respectively");
        String userInput = in.next();
        if(userInput.equalsIgnoreCase("G")) {
            //grants access to admins,usersales,userhr,or userengineering
            System.out.println("Who to grant access to - admin, usersales, userhr, userengineering?");
            String user = in.next();
            System.out.println("Which table to grant to");
            String table = in.next();
            System.out.println( "What grants? - all,select,insert,update,delete,create" + "\n" +
                                "Type in with this format, no spaces - grant1,grant2,grant3...");
            String grantsparam = in.next();
            try {
                Statement stmt = con.createStatement();
                String grantsql = "grant " + grantsparam + " on " + table + " to " + user;
                stmt.executeUpdate(grantsql);
                stmt.close();
                con.commit();
                System.out.println("Successfully granted access");
            } catch (Exception ie) {
                ie.printStackTrace();
                System.out.println("Invalid - Shutting down");
                APP.forceExit();
            }
        }else if(userInput.equalsIgnoreCase("C")){
            System.out.println("Which user?");
            String user = in.next();
            System.out.println("What access type to change to?");
            String accessType = in.next();
            //update login info
            String filepath = "C:/Users/mlgam/OneDrive/Desktop/CS Projects/CS-425/Project/ActualProject/src/logins.csv";
            String oldaccess = null;
            String pass = null;
            //read file and get all info about the user
            //then replace the line with updated access
            try{
                File file1 = new File(filepath);
                Scanner read = new Scanner(file1);
                read.nextLine(); //skip first line
                String line;
                String[] info;
                while(read.hasNextLine()){
                    line = read.nextLine();
                    info = line.split(",");
                    if(info[0].equals(user)){
                        pass = info[1];
                        oldaccess = info[2];
                    }
                }
                read.close();
                String oldLine = user + "," + pass + "," + oldaccess;
                String newLine = user + "," + pass + "," + accessType;
                //update line
                File file = new File(filepath);
                String old = "";
                BufferedReader reader = new BufferedReader(new FileReader(file));
                line = reader.readLine();
                while(line != null){
                    old += line + System.lineSeparator();
                    line = reader.readLine();
                }
                //actual replacing
                String updatedFile = old.replaceAll(oldLine, newLine);
                //rewriting
                FileWriter writer = new FileWriter(file);
                writer.write(updatedFile);
                reader.close();
                writer.close();
                System.out.println("Updated user successfully");
            }catch(Exception ie){
                ie.printStackTrace();
                System.out.println("Invalid - Shutting down");
                APP.forceExit();
            }
        }
    }

    public void salesReport(){
        //for sales and ofc admins
        if(!access.equalsIgnoreCase("Sales") && !access.equalsIgnoreCase("Admin")){
            return;
        }
        try {
            Statement stmt = con.createStatement();
            ResultSet om = stmt.executeQuery("Select model_number, amount from Inventory");
            Hashtable<String, Integer> modelAmount = new Hashtable<>();
            while (om.next()) {
                //read values in table
                String modelNumber = om.getString("model_number");
                int amt = om.getInt("amount");
                //insert into hashtable
                modelAmount.put(modelNumber, amt);
            }
            String out = "";
            ResultSet sm = stmt.executeQuery("select model.model_number, sum(ordermodels.amount) as totalSold, model.sale_price from model\n" +
                    "inner join ordermodels \n" +
                    "\ton ordermodels.model = model.model_number \n" +
                    "group by model.model_number, model.sale_price\n" +
                    "order by totalSold desc;");
            System.out.println("Model, Total Sold, Price, Stock");
            while (sm.next()) {
                //read values in table
                String modelNumber = sm.getString("model_number");
                int total = sm.getInt("totalsold");
                int price = sm.getInt("sale_price");
                int currentStock = modelAmount.getOrDefault(modelNumber,0); // gets the curretn stock, if not on there then 0
                out += modelNumber + "\t" + total + "\t" + price + "\t" + currentStock + "\n";
            }
            System.out.println(out);
        }catch(Exception ie){
            ie.printStackTrace();
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        }
    }

    public void businessReport() {
        if(!access.equalsIgnoreCase("Admin")){//only admins can view
            return;
        }
        try {
            Scanner in = new Scanner(System.in);
            System.out.println("Viewing Business Reports: " + "\n" +
                    "(P) Total Profit" + "\n" +
                    "(M) Models Statistics" + "\n" +
                    "(O) Orders stats" + "\n" +
                    "(E) Expense report");
            String choice = in.nextLine();
            if (choice.equalsIgnoreCase("P")) {
                //get profit from models sold
                //int modelsProfit = 0;
                int modelsCosts = 0;
                Statement st = con.createStatement();
                ResultSet set = st.executeQuery("select model.model_number, sum(ordermodels.amount) as totalSold, model.sale_price, inventory.costs from model\n" +
                        "inner join ordermodels \n" +
                        "\ton ordermodels.model = model.model_number \n" +
                        "inner join inventory \n" +
                        "\ton inventory.model_number = model.model_number\n" +
                        "group by model.model_number, model.sale_price, inventory.costs\n" +
                        "order by totalSold desc;");
                while (set.next()) {
                    //read values in table
                    int totalSold = set.getInt("totalsold");
                    int sale_price = set.getInt("sale_price");
                    int costs = set.getInt("costs");

                    //int profit = sale_price - costs;
                    //int totalProfit = profit * totalSold;
                    int totalCosts = costs * totalSold;
                    //modelsProfit += totalProfit;
                    modelsCosts += totalCosts;
                }
                //get salevalue
                int totalSaleValue = 0;
                ResultSet sale = st.executeQuery("select sum(sale_value) as price from orders;");
                while (sale.next()) {
                    int totalSales = sale.getInt("price");
                    totalSaleValue += totalSales;
                }
                int totalProfitFromModels = totalSaleValue - modelsCosts;
                System.out.println("Total profit from sales: " + totalProfitFromModels);

                //get costs of salary
                int costsOfSalary = 0;
                ResultSet sal = st.executeQuery("select sum(salary) as total, salary_type from employee group by salary_type;");
                while (sal.next()) {
                    int total = sal.getInt("total");
                    String type = sal.getString("salary_type");
                    int add = 0;
                    if (type.equals("Hourly")) {
                        //40 hrs per week
                        //52 weeks per year
                        add = total * 40 * 52;
                    } else {//yearly already so just add
                        add = total;
                    }
                    costsOfSalary += add;
                }
                System.out.println("Costs of Employees per year: " + costsOfSalary);
            } else if (choice.equalsIgnoreCase("M")) {
                //avg bought models
                Statement st = con.createStatement();
                ResultSet res = st.executeQuery("select model, avg(amount) as average from ordermodels group by model;");
                System.out.println("Model, avg bought");
                while(res.next()){
                    String model = res.getString("model");
                    int avgBought = res.getInt("average");
                    System.out.println(model + "\t" + avgBought);
                }
            } else if (choice.equalsIgnoreCase("O")) {
                Statement st = con.createStatement();
                ResultSet mod = st.executeQuery("select order_number, model, ordermodels.amount as bought, inventory.amount as stock from ordermodels\n" +
                        "inner join inventory \n" +
                        "\ton inventory.model_number = ordermodels.model;");
                while(mod.next()){
                    String order = mod.getString("order_number");
                    String model = mod.getString("model");
                    int bought = mod.getInt("bought");
                    int stock = mod.getInt("stock");
                    System.out.println("Order: " + order + "\t" + "Model: " + model + "\t" + "Bought: " + bought + "\t" + "Stock Left: " + stock);
                }
            } else if (choice.equalsIgnoreCase("E")) {
                System.out.println("Employee Salaries: ");
                Statement st = con.createStatement();
                ResultSet res = st.executeQuery("select first_name, last_name, salary, salary_type from employee order by salary desc;");
                while(res.next()){
                    String first = res.getString("first_name");
                    String last = res.getString("last_name");
                    int salary = res.getInt("salary");
                    String salaryType = res.getString("salary_type");
                    System.out.println(first + " " + last + "\t $" + salary + " " + salaryType);
                }
                System.out.println();
                System.out.println("Models Costs: ");
                ResultSet mod = st.executeQuery("select model_number, costs from inventory order by costs desc;");
                while(mod.next()){
                    String model = mod.getString("model_number");
                    int cost = mod.getInt("costs");
                    System.out.println(model + "\t $" + cost);
                }
            } else {
                System.out.println("Invalid choice");
            }
        }catch(Exception ie){
            ie.printStackTrace();
            System.out.println("Invalid - Shutting down");
            APP.forceExit();
        }
    }
    public String ask() {
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter the table:");
        String str = in.nextLine();
        return str;
    }
    public String[] ask1() {
        Scanner in = new Scanner(System.in);
        String[]ans = new String[3];
        System.out.println("Enter the column which you wanna change:");
        ans[0] = in.nextLine();
        System.out.println("Enter the value which you wanna update the column to (set column = ?):");
        ans[1] = in.nextLine();
        System.out.println("Please enter the id which you wanna update.");
        ans[2] = in.nextLine();
        return ans;
    }
}
