import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

//make method to drop table?

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

        //get current time
        Date currentTime = new Date();
        long time = currentTime.getTime();
        loginTime = new Timestamp(time);
    }

    public void exit(){
        //get current time of logout
        Date currentTime = new Date();
        long time = currentTime.getTime();
        logoutTime = new Timestamp(time);

        //insert info into login
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
        }
    }

    public void create() {//create table
        if(!access.equals("Admin")){//not admin, cant create table
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
        }
    }

    public void view() {//TODO select
        //sales can view customer, orders, ordermodels, Inventory
        //engineers can view model, inventory, and partial employee - made a view specifically for it, hmm how to access that
        //hr can view employee and orders - says can view employee and associated sales number hmmm
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
        if(access.equals("Sales") && (!table.equals("Customer") && !table.equals("Orders") && !table.equals("OrderModels"))){
            System.out.println("No Access");
            return;
        }else if(access.equals("Engineering") && (!table.equals("Model") && !table.equals("Inventory"))){
            System.out.println("No Access");
            return;
        }else if(access.equals("HR")){
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
                    }
                }else if(columnTypes[i - 1].equals("java.lang.String")){
                    prepstmt.setString(i, input);
                    if(table.equals("Employee")) {//if adding in to employee
                        if (i - 1 == 4) {
                            userID = input;
                        }
                        else if (i - 1 == 7) {
                            access = input;
                        }
                    }
                    if(table.equals("Orders")){
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
            if(table.equals("Orders")){
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
                    //if it is, update the value, if becomes 0 then remove
                    if(amount == modelAmount.get(model)){//delete row from inventory cuz bought all
                        //del
                        String delRow = "delete from Inventory where model_number = ?";
                        PreparedStatement ps = con.prepareStatement(delRow);
                        ps.setString(1, model);
                        updates.add(ps);
                    }else{//update the row, with new amount
                        int amountLeft = modelAmount.get(model) - amount;
                        String update = "update Inventory set amount = ? where model_number = ?";
                        PreparedStatement ps = con.prepareStatement(update);
                        ps.setInt(1, amountLeft);
                        ps.setString(2, model);
                        updates.add(ps);
                    }
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
                    if(input.equals("no")){
                        run = false;
                        break;
                    }
                }
            }
            //ask user for password if adding into employee
            if(table.equals("Employee")){
                System.out.println("Adding an employee, need to add login information as well");
                String file = "C:/Users/mlgam/OneDrive/Desktop/CS Projects/CS-425/Project/ActualProject/src/logins.csv";
                try{
                    createUser(file, userID, null, access);
                }catch(Exception ie){
                    System.out.println("Error, unable to insert");
                    return;
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
        }
    }

    public void createUser(String filepath, String username, String password, String accessType){
        if(access.equals("Admin")) { // only admins can create a user
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
            }
        }else{
            System.out.println("No access");
        }
    }

    public void update() {//TODO alter, and delete, update
        //sales can update customer
        //engineers can update model, inventory
        //hr can update employee
    }

    public void grant() {
        if(!access.equals("Admin")){ //if not admin, then cant grant
            System.out.println("No access");
            return;
        }
        Scanner in = new Scanner(System.in);
        System.out.println("Change access type of a user, or grant access? C or G respectively");
        String userInput = in.next();
        if(userInput.equals("G")) {
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
            }
        }else if(userInput.equals("C")){
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
            }
        }
    }

    public void salesReport() {//TODO
        //for sales and maybe hr??? and ofc admins
    }

    public void businessReport() {//TODO
        //view for admins or create
    }
}
