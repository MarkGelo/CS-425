import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;
import java.io.File;

public class APP {
    public static String url = "jdbc:postgresql://localhost/postgres";
    public static String filepath = "C:/Users/mlgam/OneDrive/Desktop/CS Projects/CS-425/Project/ActualProject/src/logins.csv";
    //postgresql database user login password
    public static String user;
    public static String password;
    //user login
    public static String userLogin;
    public static String passwordLogin;

    public static String accessType;
    public static Connection conn;

    public static String menu = "P: Check Privileges" + "\n" +
                                "C: Create" + "\n" +
                                "V: View" + "\n" +
                                "I: Insert" + "\n" +
                                "U: Update" + "\n" +
                                "G: Grant" + "\n" +
                                "S: Sales Report" + "\n" +
                                "B: Business Report" + "\n" +
                                "CE: Create User" + "\n" +
                                "E: Exit";

    public static void main(String[] args){
        boolean correctLogin = false;
        Scanner in = new Scanner(System.in);
        while(conn == null && !correctLogin) {
            //ask user for login
            System.out.print("Username: ");
            userLogin = in.next();
            System.out.print("Password: ");
            passwordLogin = in.next();

            //check if correct login
            correctLogin = checkLogin();
        }
        //connect to database according to those credentials
        connect();
        DBM dbm = new DBM(conn, userLogin, accessType);

        boolean run = true;
        while(run){
            System.out.println(menu);
            String userInput = in.next();
            switch (userInput) {
                case "E":
                    run = false;
                    dbm.exit();
                    break;
                case "P":
                    checkPrivileges();
                    break;
                case "C"://create
                    dbm.create();
                    break;
                case "V"://view - select
                    dbm.view();
                    break;
                case "I"://insert
                    dbm.insert();
                    break;
                case "U"://update - alter
                    dbm.update();
                    break;
                case "G"://grant
                    dbm.grant();
                    break;
                case "S"://sales report
                    dbm.salesReport();
                    break;
                case "B"://business report
                    dbm.businessReport();
                    break;
                case "CE"://create user
                    dbm.createUser(filepath, null, null, null);
                    break;
                default:
                    System.out.println("Invalid choice" + "\n" + "Choose again");
                    break;
            }
            System.out.println("--------------------------------------");
        }
    }

    public static void connect(){
        try {
            Class.forName("org.postgresql.Driver");
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            conn = DriverManager.getConnection(url, props);

            if(conn != null){
                System.out.println("Connected successfully");
                System.out.println("Username: " + userLogin + "\t Access: " + accessType);
                conn.setAutoCommit(false);
            }else{
                System.out.println("Not Connected");
            }
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }

    public static boolean checkLogin(){
        File file = new File(filepath);
        try{
            Scanner in = new Scanner(file);
            in.nextLine(); //skip first line
            String line;
            String[] info;
            while(in.hasNextLine()){ //check if given login info is correct
                line = in.nextLine();
                info = line.split(",");
                if(userLogin.equals(info[0]) && passwordLogin.equals(info[1])){
                    //get the access type and set user/password for actual database
                    accessType = info[2];
                    switch (accessType) {
                        case "Admin":
                            user = "useradmin";
                            password = "useradmin";
                            break;
                        case "HR":
                            user = "userhr";
                            password = "userhr";
                            break;
                        case "Sales":
                            user = "usersales";
                            password = "usersales";
                            break;
                        case "Engineering":
                            user = "userengineering";
                            password = "userengineering";
                            break;
                        default:  //wrong access type
                            return false;
                    }
                    return true;
                }
            }
            System.out.println("Incorrect username/password");
            return false;
        }catch(Exception ie){
            //ie.printStackTrace();
            return false;
        }
    }

    public static void checkPrivileges(){
        System.out.println("Your privileges: ");
        switch (accessType) {
            case "Admin":
                System.out.println("Create a new Employee" + "\n" +
                        "Set up tables" + "\n" +
                        "Grant access" + "\n" +
                        "Access and create Business Report" + "\n");
                break;
            case "Sales":
                System.out.println("View and update customer" + "\n" +
                        "Create an order" + "\n" +
                        "Access sales report" + "\n");
                break;
            case "Engineering":
                System.out.println("Access and update model" + "\n" +
                        "Access and update inventory" + "\n" +
                        "Limited view to employee information" + "\n");
                break;
            case "HR":
                System.out.println("Access and update employee information" + "\n" +
                        "View of employee and associated sales number" + "\n");
                break;
            default:
                System.out.println("How did you get this far" + "\n");
                break;
        }
    }

    public static void forceExit(){
        System.exit(1);
    }
}
