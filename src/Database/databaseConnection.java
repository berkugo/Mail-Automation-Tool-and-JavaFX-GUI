package Database;


import Login.secondPage.Company;
import Users.Users;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class databaseConnection {


    private Connection con;
    private Statement st;
    private ResultSet rs;
    private String server_adress = "160.153.18.110";
    private String username = "mailappadmin";
    private String password = "admin";
    private String databasename = "mailapp";
    private Thread database_thread;
    private boolean logged = false;
    private boolean operation;



    public databaseConnection()
    {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
    public boolean openConnection()
    {

        try
        {
            this.con = DriverManager.getConnection("jdbc:mysql://"+server_adress+"/"+databasename, username, password);
            st = con.createStatement();
            return true;
        }
        catch (SQLException ex)
        {
            return false;
        }

    }
    public ArrayList<String[]> getSalesTeam() throws ExecutionException, InterruptedException, SQLException
    {
        Task<ArrayList<String[]>> st = new Task<ArrayList<String[]>>()
        {
            @Override
            protected ArrayList<String[]> call() throws Exception {
                ArrayList<String[]> lst = new ArrayList<>();

                if(!this.isDone()) {
                    String query = "SELECT * FROM EMPLOYEES_INFORMATION";
                    rs = databaseConnection.this.st.executeQuery(query);
                    while (rs.next()) {
                       String[] str = {String.valueOf(rs.getInt("Bitrix_ID")), rs.getString("Name"), rs.getString("Password"), rs.getString("Email")};
                       lst.add(str);
                    }
                }
                this.done();
                //databaseConnection.this.con.close();
                return lst;
            }
        };
        new Thread(st).start();
        return st.get();
    }

    public Task<Boolean> resetAllData() throws ExecutionException, InterruptedException {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                Statement st = con.createStatement();
                String query = "DELETE FROM COMPANIES";
                st.execute(query);
                query = "UPDATE GENERAL_INFO SET iteration = 0";
                st.execute(query);
                con.close();
                return true;
            }
        };
        new Thread(task).start();
        return task;
    }
    public ArrayList<String[]> getTemplates(int id, String country) throws ExecutionException, InterruptedException, SQLException {
        Task<ArrayList<String[]>> st = new Task<ArrayList<String[]>>() {
            @Override
            protected ArrayList<String[]> call() throws Exception {
                ArrayList<String[]> lst = new ArrayList<>();

                String query;
                if (!this.isDone()) {

                    query = "SELECT * FROM TEMPLATES WHERE bitrixid='"+id+"' AND country='"+country+"'";
                    rs = databaseConnection.this.st.executeQuery(query);
                    while (rs.next()) {
                        String[] str = {String.valueOf(rs.getInt("id")), String.valueOf(rs.getInt("defaulttemp")), rs.getString("template"), rs.getString("country"), rs.getString("title")};
                        lst.add(str);
                    }
                }
                this.done();
                //databaseConnection.this.con.close();
                return lst;
            }
        };
        new Thread(st).start();
        return st.get();
    }
    public Task<Boolean> updateCompany(String id, String email, String state, String country, String phone) throws ExecutionException, InterruptedException, SQLException {
         Task<Boolean> task = new Task<Boolean>() {
             @Override
             protected Boolean call() throws Exception {

                 if(!this.isDone())
                 {
                     Statement st = con.createStatement();
                     String query = "UPDATE COMPANIES SET company_email = '" + email + "', phone = '" + phone + "', state = '" + state + "', country = '" + country + "' WHERE id = " + id;
                     try {
                         System.out.println(st.execute(query));
                         con.close();
                     }
                     catch (SQLException ex)
                     {
                         ex.printStackTrace();
                     }

                     this.succeeded();
                     return true;
                 }
                 else {
                     this.failed();
                     return false;
                 }
             }
         };
         new Thread(task).start();
         return task;
    }
    public Task<Boolean> getCompanies(String date, boolean state) throws ExecutionException, InterruptedException {
        if(state)
        {
            Task<Boolean> st = new Task<Boolean>() {
                @Override
                public Boolean call() throws Exception {

                    HashMap<String, String> hashMap = new HashMap<>();
                    String query = "SELECT * FROM COMPANIES WHERE lastcontacted = '" + date.trim() + "' OR lastcontacted = 'Not yet'";
                    if (!this.isDone()) {
                        Statement st = con.createStatement();
                        ResultSet rst = st.executeQuery(query);
                        Company.list.clear();
                        while (rst.next()) {
                            try {
                                hashMap.put("id", String.valueOf(rst.getInt("id")));
                                hashMap.put("companyName", rst.getString("company_name"));
                                hashMap.put("respperson", rst.getString("responsible_person"));
                                hashMap.put("country", rst.getString("country"));
                                hashMap.put("status", rst.getString("status"));
                                hashMap.put("company_email", rst.getString("company_email"));
                                hashMap.put("state", rst.getString("state"));
                                hashMap.put("lastcontacted", rst.getString("lastcontacted"));
                                new Company(rst.getInt("id"), rst.getString("company_name"), Integer.parseInt(rst.getString("responsible_person")), getSalesTeam(), rst.getString("company_email"), rst.getString("state"), rst.getString("status"), rst.getString("lastcontacted"), rst.getString("country"), rst.getString("phone"));

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                        databaseConnection.this.con.close();

                    }
                    this.succeeded();
                    return true;
                }
            };
            new Thread(st).start();
            return st;
        }
        else
        {
            Task<Boolean> st = new Task<Boolean>() {
                @Override
                public Boolean call() throws Exception {

                    HashMap<String, String> hashMap = new HashMap<>();
                    String query = "SELECT * FROM COMPANIES WHERE lastcontacted = '" + date.trim() + "'";
                    if (!this.isDone()) {
                        Statement st = con.createStatement();
                        ResultSet rst = st.executeQuery(query);
                        Company.list.clear();
                        while (rst.next()) {
                            try {
                                hashMap.put("id", String.valueOf(rst.getInt("id")));
                                hashMap.put("companyName", rst.getString("company_name"));
                                hashMap.put("respperson", rst.getString("responsible_person"));
                                hashMap.put("country", rst.getString("country"));
                                hashMap.put("status", rst.getString("status"));
                                hashMap.put("company_email", rst.getString("company_email"));
                                hashMap.put("state", rst.getString("state"));
                                hashMap.put("lastcontacted", rst.getString("lastcontacted"));
                                new Company(rst.getInt("id"), rst.getString("company_name"), Integer.parseInt(rst.getString("responsible_person")), getSalesTeam(), rst.getString("company_email"), rst.getString("state"), rst.getString("status"), rst.getString("lastcontacted"), rst.getString("country"), rst.getString("phone"));

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                        databaseConnection.this.con.close();

                    }
                    this.succeeded();
                    return true;
                }
            };
            new Thread(st).start();
            return st;
        }
    }
   public int updateStatus(int id, String date) throws ExecutionException, InterruptedException {
       Task<Integer> st = new Task<Integer>()
       {
           @Override
           protected Integer call() throws Exception {

               if(!this.isDone()) {
                   String query = "UPDATE COMPANIES SET status = 'Sent', lastcontacted ='" + date + "'  WHERE id=" + id;
                   try {
                       databaseConnection.this.st.execute(query);

                   }
                   catch (SQLException ex)
                   {
                       System.out.println(ex);
                   }
               }
               databaseConnection.this.con.close();
               this.succeeded();
               return 0;
           }
       };
       new Thread(st).start();
       return st.get();
   }
    public int saveCompanies(int id, String company_name, String responsible_person, String country, String state, String status, String company_email, String templates, String phone) throws ExecutionException, InterruptedException {
        Task<Integer> st = new Task<Integer>()
        {
            @Override
            protected Integer call() throws Exception {

                if(!this.isDone()) {
                    String query = "INSERT INTO COMPANIES (id, company_name, responsible_person, country, status, company_email, state, phone) VALUES('"
                            + id + "', '" + company_name + "', '" + responsible_person + "', '" + country + "', '" + status + "', '" + company_email + "', '" + state + "', '" + phone + "')";
                    try {
                        databaseConnection.this.st.execute(query);

                    }
                    catch (SQLException ex)
                    {
                        System.out.println(ex);
                    }
                }
                databaseConnection.this.con.close();
                this.succeeded();
                return 0;
            }
        };
        new Thread(st).start();
        return st.get();
    }
    public int getIteration() throws ExecutionException, InterruptedException, SQLException
    {
        Task<Integer> st = new Task<Integer>()
        {
            @Override
            protected Integer call() throws Exception {
                int iter = 0;

                if(!this.isDone()) {
                    String query = "SELECT Iteration FROM GENERAL_INFO";
                    rs = databaseConnection.this.st.executeQuery(query);
                    while (rs.next()) {
                        iter = rs.getInt("Iteration");
                    }
                    this.done();
                    return iter;
                }
                databaseConnection.this.con.close();
                this.done();
                return iter;
            }
        };
        new Thread(st).start();
        return st.get();
    }
    public boolean setIteration(final int iter) throws ExecutionException, InterruptedException, SQLException
    {
        Task<Boolean> st = new Task<Boolean>()
        {
            @Override
            protected Boolean call() throws Exception {

                if(!this.isDone()) {
                    try {

                        String query = "UPDATE GENERAL_INFO SET Iteration=" + iter;
                        databaseConnection.this.st.execute(query);
                    }
                    catch (SQLException ex)
                    {
                        System.out.println(ex);
                    }

                }
                databaseConnection.this.con.close();
                this.done();
                return true;
            }
        };
        new Thread(st).start();
        return st.get();
    }
    public boolean checkForUsers(String username, String pass) throws InterruptedException, ExecutionException {

        this.operation = true;
        Task<Boolean> task = this.executeTask(username, pass);
        this.database_thread = new Thread(task);
        this.database_thread.start();
        return task.get();

    }
    private Task<Boolean> executeTask(String username, String pass)
    {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {


                try {

                    if (!this.isCancelled()) {
                        String query = "SELECT * FROM USERS_ADMIN WHERE username =" + "'" + username + "'" + " AND " + "pass=" + "'" + pass + "'";
                        rs = st.executeQuery(query);
                        boolean flag = false;
                        while (rs.next()) {

                            Users ref = new Users(rs.getInt("id"), username, pass);
                            System.out.println(ref);
                            flag = true;
                            logged = true;
                            return true;
                        }
                        if (!flag) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.getButtonTypes().setAll(new ButtonType("Try Again"));
                                    alert.setTitle("Login Error");
                                    alert.setHeaderText("");
                                    alert.setContentText("Your password or username is wrong.");
                                    alert.showAndWait();

                                }
                            });
                            return false;

                        }
                        this.cancel();
                    }
                }
                catch(SQLException e){
                    e.printStackTrace();
                }

                return true;

            }

        };
        return task;
    }

}
