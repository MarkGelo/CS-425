import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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

    public void view() {
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
            //TODO if inserting an employee, add userID to login csv
            //get parameters
            System.out.println("Insert values: ");
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
                }else if(columnTypes[i - 1].equals("java.lang.Integer")){
                    int temp = Integer.parseInt(input);
                    prepstmt.setInt(i, temp);
                }else{
                    //Wrong column type
                }
            }
            //statement finished
            prepstmt.executeUpdate();
            prepstmt.close();
            con.commit();
            System.out.println("Insert successful");
        }catch(Exception ie){
            ie.printStackTrace();
        }
    }

    public void update() {
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
            //TODO update login csv with updated access
        }
    }

    public void salesReport() {
    }

    public void businessReport() {
    }
}
