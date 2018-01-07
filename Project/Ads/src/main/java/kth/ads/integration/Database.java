package kth.ads.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database {
    
    private static final String TABLE_NAME_USER = "aduser";
    private static final String TABLE_NAME_AD = "advertisement";
    private PreparedStatement createAccount;
    private PreparedStatement createAd;
    private PreparedStatement checkAccount;
    private PreparedStatement checkEmail;
    private PreparedStatement getLastId;
    private PreparedStatement getListAds;
    private PreparedStatement getAd;
    
    public Database() {
        try {
            Connection connection = this.connection();
            this.prepareStatements(connection);
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
        }
    }
    
    private Connection connection() throws ClassNotFoundException, SQLException {
        try{
            String dbURL = "jdbc:derby://localhost:1527/sample;create=false;user=app;password=app";
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            return DriverManager.getConnection(dbURL);    
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    private void prepareStatements(Connection connection) throws SQLException {
        this.createAccount = connection.prepareStatement("INSERT INTO " + TABLE_NAME_USER + " VALUES (?, ?)");
        this.createAd = connection.prepareStatement("INSERT INTO " + TABLE_NAME_AD + " (title, description, price, category, useremail, id, location) VALUES (?, ?, ?, ?, ?, ?, ?)");
        this.checkAccount = connection.prepareStatement("SELECT * from " + TABLE_NAME_USER + " WHERE email = ? AND password = ?");
        this.checkEmail = connection.prepareStatement("SELECT * from " + TABLE_NAME_USER + " WHERE email = ?");
        this.getLastId = connection.prepareStatement("SELECT MAX(ID) FROM " + TABLE_NAME_AD);
        this.getListAds = connection.prepareStatement("SELECT * FROM " + TABLE_NAME_AD);
        this.getAd = connection.prepareStatement("SELECT * FROM " + TABLE_NAME_AD + " WHERE ID = ?");
    }
    
    public boolean createAccount(String email, String password) {
        try {
            this.checkEmail.setString(1, email);
            ResultSet result = this.checkEmail.executeQuery();
            if (result.next()) {
                // Email already used
                return false;
            }
            this.createAccount.setString(1, email);
            this.createAccount.setString(2, password);
            int rows = this.createAccount.executeUpdate();
            return rows == 1;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }
    
    public boolean isAccount(String email, String password) {
        ResultSet result = null;
        try {
            this.checkAccount.setString(1, email);
            this.checkAccount.setString(2, password);
            result = this.checkAccount.executeQuery();
            if (result.next()) {
                // Ok, match
                return true;
            }
        } catch (SQLException sqle) {
                sqle.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean addAd(String title, String description, String price, String category, String user, String location) {
        try {
            // Id generator
            int id = 0;
            ResultSet result = this.getLastId.executeQuery();
            if (result.next()) {
                id = result.getInt(1) + 1;
            }
            else{
                return false;
            }
            this.createAd.setString(1, title);
            this.createAd.setString(2, description);
            this.createAd.setString(3, price);
            this.createAd.setString(4, category);
            this.createAd.setString(5, user);
            this.createAd.setString(6, Integer.toString(id));
            this.createAd.setString(7, location);
            int rows = this.createAd.executeUpdate();
            return rows == 1;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }
    
    public List <String []> getAds(){
        try{
            ResultSet result = this.getListAds.executeQuery();
            List <String []> ads = new ArrayList<>();
            while (result.next()) {
                String title = result.getString("title");
                String description = result.getString("description");
                String price = result.getString("price");
                String category = result.getString("category");
                String email = result.getString("useremail");
                String location = result.getString("location");
                String id = result.getString("id");
                String [] ad = new String[7];
                ad[0] = title;
                ad[1] = description;
                ad[2] = price;
                ad[3] = category;
                ad[4] = email;
                ad[5] = location;
                ad[6] = id;
                ads.add(ad);
            }
            return ads;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public String [] getAd(String id){
        try{
            this.getAd.setString(1, id);
            ResultSet result = this.getAd.executeQuery();
            result.next();
            String title = result.getString("title");
            String description = result.getString("description");
            String price = result.getString("price");
            String category = result.getString("category");
            String email = result.getString("useremail");
            String location = result.getString("location");
            String [] ad = new String[7];
            ad[0] = title;
            ad[1] = description;
            ad[2] = price;
            ad[3] = category;
            ad[4] = email;
            ad[5] = location;
            ad[6] = id;
            return ad;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
}