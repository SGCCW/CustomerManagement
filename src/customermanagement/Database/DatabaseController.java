package customermanagement.Database;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;
import customermanagement.Models.Appointment;
import customermanagement.Models.Customer;
//import Model.User;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author collin.walker
 */
public class DatabaseController {
    //private final String dbURL = "jdbc:sqlserver://DB01\\Support;user=ProjectTrackerApp;password=ekQpntAjd9taQX9n";
    private String dbServer = "";
    private String dbDatabase = "";
    private String dbUser = "";
    private String dbUserPass = "";
    private String loggedInUser;
    private int loggedInUserId;
    Connection dbConn = null;
    
    
    public DatabaseController(){
        System.out.println("An empty database controller has been made."
                + " Set the server and username to use it.");
    }
    public DatabaseController(String server, String database, String user, String pass){
        this.dbServer = server;
        this.dbDatabase = database;
        this.dbUser = user;
        this.dbUserPass = pass;
        
        this.connect();
    }
    
    public String getServer(){
        return this.dbServer;
    }
    public String getUser(){
        return this.dbUser;
    }
    private String getUserPass(){
        return this.dbUserPass;
    }
    public String getLoggedInUser(){
        return this.loggedInUser;
    }
    public int getLoggedInUserId(){
        return this.loggedInUserId;
    }
    private String getURLString(){
        String dbURL = "jdbc:mysql://";
        dbURL += dbServer + "/";
        dbURL += dbDatabase;
        
        return dbURL;
    }
    
    public boolean isConnected(){
        if(this.dbConn != null){
            return true;
        }
        return false;
    }
    
    public void setServer(String server){
        this.dbServer = server;
    }
    public void setUser(String user){
        this.dbUser = user;
    }
    public void setPass(String pass){
        this.dbUserPass = pass;
    }
    public void setLoggedInUser(String user){
        this.loggedInUser = user;
    }
    public void setLoggedInUserId(int id){
        this.loggedInUserId = id;
    }
    
    public boolean connect(){
        String dbURL = this.getURLString();
        
        try {
            this.dbConn = DriverManager.getConnection(  dbURL,
                                                        this.dbUser,
                                                        this.dbUserPass);
        } catch (SQLException ex) {
            this.dbConn = null;
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (this.dbConn != null) {
            System.out.println("Connected to " + this.getServer());
            return true;
        }
        return false;
    }
    
    public boolean refreshConnection(){
        try {
            this.dbConn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(this.connect()){
            System.out.println("DB Connection Refresh Successful");
            return true;
        }
        return false;
    }
    
    public boolean disconnect(){
        try {
            this.dbConn.close();
            System.out.println("Disconnecting from " + this.dbServer);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    /*
    
            CUSTOM QUERIES BELOW
    _______________________________________
    
    */
    
    public int authenticateUser(String username, String password){
        PreparedStatement authUserStatement = null;
        ResultSet rs = null;
        boolean isauthed = false;
        int userid = -1;
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return -1;
        }
        
        try{
            this.dbConn.setAutoCommit(false);
            String sqlAuthUser;
            if(password.equals("")){
                sqlAuthUser =   "SELECT CASE\n" +
                                "           WHEN password IS NULL THEN userID ELSE 0 " +
                                "       END AS UserID\n" +
                                "FROM	user\n" +
                                "WHERE	userName = ?\n" +
                                "AND    active = '1'";
                authUserStatement = this.dbConn.prepareStatement(sqlAuthUser);
                authUserStatement.setString(1, username);
            }
            else{
                sqlAuthUser =   "SELECT CASE\n" +
                                "           WHEN password = ? THEN userID ELSE 0 " +
                                "       END AS UserID\n" +
                                "FROM	user\n" +
                                "WHERE	userName = ?\n" +
                                "AND    active = '1'";
                authUserStatement = this.dbConn.prepareStatement(sqlAuthUser);
                authUserStatement.setString(1, password);
                authUserStatement.setString(2, username);
            }
            
            
            
            rs = authUserStatement.executeQuery();
            
            isauthed = rs.next();
            if(isauthed){
                userid = rs.getInt("userID");
                if(userid != 0){
                    System.out.println("User " + username + " has authed successfully.");
                }
            }
            
            
            System.out.println("testing");
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return -1;
                }
                return -1;
            }
        }
        finally{
            try{
                if (authUserStatement != null){
                    authUserStatement.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
            this.loggedInUserId = userid;
        }
        System.out.println(userid);
        return userid;
    }
    
    public Optional<Integer> getCountryID(String country){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return Optional.empty();
        }
        
        PreparedStatement stmtGetCountryID = null;
        ResultSet rs = null;
        Integer countryid = null;
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetCountryID =    "SELECT	countryId\n" +
                                        "FROM	country\n" +
                                        "WHERE	country = ?;";
            stmtGetCountryID = this.dbConn.prepareStatement(sqlGetCountryID);
            stmtGetCountryID.setString(1, country);
            rs = stmtGetCountryID.executeQuery();
            
            while(rs.next()){
                countryid = rs.getInt("countryId");
                System.out.println("GetCountryID().countryid = " + countryid);
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return Optional.empty();
                }
                return Optional.empty();
            }
        }
        finally{
            try{
                if (stmtGetCountryID != null){
                    stmtGetCountryID.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        if(countryid != null){
            System.out.println("GetCountryID().return.countryid = " + countryid);
            return Optional.of(countryid);
        }
        return Optional.empty();
    }
    public Optional<Integer> getCityID(String city){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return Optional.empty();
        }
        
        PreparedStatement stmtGetCityID = null;
        ResultSet rs = null;
        Integer cityid = null;
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetCountryID =    "SELECT	cityId\n" +
                                        "FROM	city\n" +
                                        "WHERE	city = ?;";
            stmtGetCityID = this.dbConn.prepareStatement(sqlGetCountryID);
            stmtGetCityID.setString(1, city);
            rs = stmtGetCityID.executeQuery();
            
            while(rs.next()){
                cityid = rs.getInt("cityId");
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return Optional.empty();
                }
                return Optional.empty();
            }
        }
        finally{
            try{
                if (stmtGetCityID != null){
                    stmtGetCityID.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        if(cityid != null){
            return Optional.of(cityid);
        }
        return Optional.empty();
    }
    
    public Optional<Integer> getAddressID(String address){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return Optional.empty();
        }
        
        PreparedStatement stmtGetAddressID = null;
        ResultSet rs = null;
        Integer addressid = null;
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetAddressID =    "SELECT	addressId\n" +
                                        "FROM	address\n" +
                                        "WHERE	address = ?\n";
            stmtGetAddressID = this.dbConn.prepareStatement(sqlGetAddressID);
            stmtGetAddressID.setString(1, address);
            rs = stmtGetAddressID.executeQuery();
            
            while(rs.next()){
                addressid = rs.getInt("addressId");
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return Optional.empty();
                }
                return Optional.empty();
            }
        }
        finally{
            try{
                if (stmtGetAddressID != null){
                    stmtGetAddressID.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        if(addressid != null){
            return Optional.of(addressid);
        }
        return Optional.empty();
    }
    
    public Optional<Integer> getCustomerID(String name){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return Optional.empty();
        }
        
        PreparedStatement stmtGetCustomerID = null;
        ResultSet rs = null;
        Integer customerid = null;
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetCountryID =    "SELECT	customerId\n" +
                                        "FROM	customer\n" +
                                        "WHERE	customerName = ?\n";
            stmtGetCustomerID = this.dbConn.prepareStatement(sqlGetCountryID);
            stmtGetCustomerID.setString(1, name);
            rs = stmtGetCustomerID.executeQuery();
            
            while(rs.next()){
                customerid = rs.getInt("customerId");
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return Optional.empty();
                }
                return Optional.empty();
            }
        }
        finally{
            try{
                if (stmtGetCustomerID != null){
                    stmtGetCustomerID.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        if(customerid != null){
            return Optional.of(customerid);
        }
        return Optional.empty();
    }
    
    public Optional<Integer> getAppointmentID(String title){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return Optional.empty();
        }
        
        PreparedStatement stmtGetAppointmentID = null;
        ResultSet rs = null;
        Integer appointmentid = null;
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetAppointmentID =    "SELECT	appointmentId\n" +
                                            "FROM	appointment\n" +
                                            "WHERE	title = ?\n";
            stmtGetAppointmentID = this.dbConn.prepareStatement(sqlGetAppointmentID);
            stmtGetAppointmentID.setString(1, title);
            rs = stmtGetAppointmentID.executeQuery();
            
            while(rs.next()){
                appointmentid = rs.getInt("appointmentId");
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return Optional.empty();
                }
                return Optional.empty();
            }
        }
        finally{
            try{
                if (stmtGetAppointmentID != null){
                    stmtGetAppointmentID.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        if(appointmentid != null){
            return Optional.of(appointmentid);
        }
        return Optional.empty();
    }
    
    public ArrayList<Customer> getCustomers(){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return new ArrayList<Customer>();
        }
        
        PreparedStatement stmtGetCustomers = null;
        ResultSet rs = null;
        ArrayList<Customer> customers = new ArrayList();
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetCustomers =    "SELECT     c.customerId,\n" +
                                        "           c.customerName,\n" +
                                        "           a.address,\n" +
                                        "           a.address2,\n" +
                                        "           ct.city,\n" +
                                        "           ctr.country,\n" +
                                        "           a.postalCode,\n" +
                                        "           a.phone,\n" +
                                        "           c.active\n" +
                                        "FROM       customer c JOIN\n" +
                                        "           address a ON c.addressId = a.addressId JOIN\n" +
                                        "           city ct ON a.cityId = ct.cityId JOIN\n" +
                                        "           country ctr ON ct.countryId = ctr.countryId;";
            stmtGetCustomers = this.dbConn.prepareStatement(sqlGetCustomers);
            rs = stmtGetCustomers.executeQuery();
            
            Customer tmpcustomer;
            int customerid;
            String customerName;
            String address;
            String address2;
            String city;
            String country;
            String postalCode;
            String phone;
            boolean active;
            while(rs.next()){
                customerid = rs.getInt("customerId");
                customerName = rs.getString("customerName");
                address = rs.getString("address");
                address2 = rs.getString("address2");
                city = rs.getString("city");
                country = rs.getString("country");
                postalCode = rs.getString("postalCode");
                phone = rs.getString("phone");
                active = rs.getBoolean("active");
                
                tmpcustomer = new Customer( customerid,
                                            customerName,
                                            address,
                                            address2,
                                            city,
                                            country,
                                            postalCode,
                                            phone,
                                            active);
                customers.add(tmpcustomer);
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return new ArrayList<Customer>();
                }
                return new ArrayList<Customer>();
            }
        }
        finally{
            try{
                if (stmtGetCustomers != null){
                    stmtGetCustomers.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        return customers;
    }
    
    public Optional<Customer> getCustomer(int id){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return Optional.empty();
        }
        
        PreparedStatement stmtGetCustomer = null;
        ResultSet rs = null;
        Customer customer = new Customer();
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetCustomer =    "SELECT     c.customerId,\n" +
                                        "           c.customerName,\n" +
                                        "           a.address,\n" +
                                        "           a.address2,\n" +
                                        "           ct.city,\n" +
                                        "           ctr.country,\n" +
                                        "           a.postalCode,\n" +
                                        "           a.phone,\n" +
                                        "           c.active\n" +
                                        "FROM       customer c JOIN\n" +
                                        "           address a ON c.addressId = a.addressId JOIN\n" +
                                        "           city ct ON a.cityId = ct.cityId JOIN\n" +
                                        "           country ctr ON ct.countryId = ctr.countryId\n" +
                                        "WHERE      c.customerId = ?;";
            stmtGetCustomer = this.dbConn.prepareStatement(sqlGetCustomer);
            stmtGetCustomer.setInt(1, id);
            rs = stmtGetCustomer.executeQuery();
            
            Customer tmpcustomer;
            int customerid;
            String customerName;
            String address;
            String address2;
            String city;
            String country;
            String postalCode;
            String phone;
            boolean active;
            while(rs.next()){
                customerid = rs.getInt("customerId");
                customerName = rs.getString("customerName");
                address = rs.getString("address");
                address2 = rs.getString("address2");
                city = rs.getString("city");
                country = rs.getString("country");
                postalCode = rs.getString("postalCode");
                phone = rs.getString("phone");
                active = rs.getBoolean("active");
                
                tmpcustomer = new Customer( customerid,
                                            customerName,
                                            address,
                                            address2,
                                            city,
                                            country,
                                            postalCode,
                                            phone,
                                            active);
                customer = tmpcustomer;
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return Optional.empty();
                }
                return Optional.empty();
            }
        }
        finally{
            try{
                if (stmtGetCustomer != null){
                    stmtGetCustomer.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        return Optional.of(customer);
    }
    
    public ArrayList<Appointment> getAppointments(LocalDateTime starting, LocalDateTime ending){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return new ArrayList<Appointment>();
        }
        
        PreparedStatement stmtGetAppointments = null;
        ResultSet rs = null;
        ArrayList<Appointment> appointments = new ArrayList();
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetAppointments =     "SELECT     appointmentId,\n" +
                                            "           customerId,\n" +
                                            "           userId,\n" +
                                            "           title,\n" +
                                            "           description,\n" +
                                            "           location,\n" +
                                            "           type,\n" +
                                            "           start,\n" +
                                            "           end\n" +
                                            "FROM       appointment\n" +
                                            "WHERE      start >= ?" +
                                            "AND        end <= ?;";
            stmtGetAppointments = this.dbConn.prepareStatement(sqlGetAppointments);
            stmtGetAppointments.setTimestamp(1, Timestamp.valueOf(starting));
            stmtGetAppointments.setTimestamp(2, Timestamp.valueOf(ending));
            rs = stmtGetAppointments.executeQuery();
            
            Appointment appointment;
            int appointmentid;
            int customerid;
            int userid;
            String title;
            String description;
            String location;
            String type;
            ZonedDateTime start;
            ZonedDateTime end;
            while(rs.next()){
                appointmentid = rs.getInt("appointmentId");
                customerid = rs.getInt("customerId");
                userid = rs.getInt("userId");
                title = rs.getString("title");
                description = rs.getString("description");
                location = rs.getString("location");
                type = rs.getString("type");
                start = rs.getTimestamp("start").toLocalDateTime().atZone(ZoneId.systemDefault());
                end = rs.getTimestamp("end").toLocalDateTime().atZone(ZoneId.systemDefault());
                Optional<Customer> customer = getCustomer(customerid);
                if(customer.isPresent()){
                    appointment = new Appointment(   appointmentid,
                                                        customer.get(),
                                                        userid,
                                                        title,
                                                        description,
                                                        location,
                                                        type,
                                                        start,
                                                        end);
                }
                else{
                    appointment = new Appointment(   appointmentid,
                                                        new Customer(),
                                                        userid,
                                                        title,
                                                        description,
                                                        location,
                                                        type,
                                                        start,
                                                        end);
                }
                
                appointments.add(appointment);
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return new ArrayList<Appointment>();
                }
                return new ArrayList<Appointment>();
            }
        }
        finally{
            try{
                if (stmtGetAppointments != null){
                    stmtGetAppointments.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        return appointments;
    }
    
    public ArrayList<Appointment> getAppointments(){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made. Connect and try again.");
            return new ArrayList<Appointment>();
        }
        
        PreparedStatement stmtGetAppointments = null;
        ResultSet rs = null;
        ArrayList<Appointment> appointments = new ArrayList();
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlGetAppointments =     "SELECT     appointmentId,\n" +
                                            "           customerId,\n" +
                                            "           userId,\n" +
                                            "           title,\n" +
                                            "           description,\n" +
                                            "           location,\n" +
                                            "           type,\n" +
                                            "           start,\n" +
                                            "           end\n" +
                                            "FROM       appointment\n;";
            stmtGetAppointments = this.dbConn.prepareStatement(sqlGetAppointments);
            rs = stmtGetAppointments.executeQuery();
            
            Appointment appointment;
            int appointmentid;
            int customerid;
            int userid;
            String title;
            String description;
            String location;
            String type;
            ZonedDateTime start;
            ZonedDateTime end;
            while(rs.next()){
                appointmentid = rs.getInt("appointmentId");
                customerid = rs.getInt("customerId");
                userid = rs.getInt("userId");
                title = rs.getString("title");
                description = rs.getString("description");
                location = rs.getString("location");
                type = rs.getString("type");
                start = rs.getTimestamp("start").toLocalDateTime().atZone(ZoneId.systemDefault());
                end = rs.getTimestamp("end").toLocalDateTime().atZone(ZoneId.systemDefault());
                Optional<Customer> customer = getCustomer(customerid);
                if(customer.isPresent()){
                    appointment = new Appointment(   appointmentid,
                                                        customer.get(),
                                                        userid,
                                                        title,
                                                        description,
                                                        location,
                                                        type,
                                                        start,
                                                        end);
                }
                else{
                    appointment = new Appointment(   appointmentid,
                                                        new Customer(),
                                                        userid,
                                                        title,
                                                        description,
                                                        location,
                                                        type,
                                                        start,
                                                        end);
                }
                
                appointments.add(appointment);
            }
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return new ArrayList<Appointment>();
                }
                return new ArrayList<Appointment>();
            }
        }
        finally{
            try{
                if (stmtGetAppointments != null){
                    stmtGetAppointments.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        return appointments;
    }
    
    
    public int addCountry(String country){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return -1;
        }
        
        int countryid = -1;
        PreparedStatement stmtAddCountry = null;
        try{
            this.dbConn.setAutoCommit(false);
            String sqlAddCountry =  "INSERT INTO country (	country,\n" +
                                    "				createDate,\n" +
                                    "				createdBy,\n" +
                                    "				lastUpdate,\n" +
                                    "                           lastUpdateBy )\n" +
                                    "VALUES (?, ?, ?, ?, ?);";
            
            stmtAddCountry = this.dbConn.prepareStatement(sqlAddCountry);
            stmtAddCountry.setString(1, country);
            stmtAddCountry.setTimestamp(2, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddCountry.setString(3, this.loggedInUser);
            stmtAddCountry.setTimestamp(4, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddCountry.setString(5, this.loggedInUser);
            
            stmtAddCountry.execute();
        }
        catch(SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return countryid;
                }
                return countryid;
            }
        }
        finally{
            try{
                if (stmtAddCountry != null){
                    stmtAddCountry.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
            
            countryid = this.getCountryID(country).get().intValue();
        }
        
        return countryid;
    }
    
    public int addCity(String city, int countryid){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return -1;
        }
        
        int cityid = -1;
        PreparedStatement stmtAddCity = null;
        try{
            this.dbConn.setAutoCommit(false);
            String sqlAddCity =     "INSERT INTO city (	city,\n" +
                                    "                   countryId,\n" +
                                    "			createDate,\n" +
                                    "			createdBy,\n" +
                                    "			lastUpdate,\n" +
                                    "                   lastUpdateBy )\n" +
                                    "VALUES (?, ?, ?, ?, ?, ?);";
            
            stmtAddCity = this.dbConn.prepareStatement(sqlAddCity);
            stmtAddCity.setString(1, city);
            stmtAddCity.setInt(2, countryid);
            stmtAddCity.setTimestamp(3, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddCity.setString(4, this.loggedInUser);
            stmtAddCity.setTimestamp(5, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddCity.setString(6, this.loggedInUser);
            
            stmtAddCity.execute();
        }
        catch(SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return cityid;
                }
                return cityid;
            }
        }
        finally{
            try{
                if (stmtAddCity != null){
                    stmtAddCity.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
            
            cityid = this.getCityID(city).get().intValue();
        }
        
        return cityid;
    }
    
    public int addAddress(  String address,
                            String address2,
                            int cityid,
                            String postalcode,
                            String phone){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return -1;
        }
        
        int addressid = -1;
        PreparedStatement stmtAddAddress = null;
        try{
            this.dbConn.setAutoCommit(false);
            String sqlAddAddress =      "INSERT INTO address (	address,\n" +
                                        "                       address2,\n" +
                                        "			cityId,\n" +
                                        "			postalCode,\n" +
                                        "			phone,\n" +
                                        "			createDate,\n" +
                                        "			createdBy,\n" +
                                        "			lastUpdate,\n" +
                                        "                       lastUpdateBy)\n" +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            
            stmtAddAddress = this.dbConn.prepareStatement(sqlAddAddress);
            stmtAddAddress.setString(1, address);
            stmtAddAddress.setString(2, address2);
            stmtAddAddress.setInt(3, cityid);
            stmtAddAddress.setString(4, postalcode);
            stmtAddAddress.setString(5, phone);
            stmtAddAddress.setTimestamp(6, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddAddress.setString(7, this.loggedInUser);
            stmtAddAddress.setTimestamp(8, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddAddress.setString(9, this.loggedInUser);
            
            stmtAddAddress.execute();
        }
        catch(SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return addressid;
                }
                return addressid;
            }
        }
        finally{
            try{
                if (stmtAddAddress != null){
                    stmtAddAddress.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
            
            addressid = this.getAddressID(address).get().intValue();
        }
        
        return addressid;
    }
    
    
    
    
    public int addCustomer(Customer customer){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return -1;
        }
        
        int customerid = -1;
        PreparedStatement stmtAddCustomer = null;
        try{
            this.dbConn.setAutoCommit(false);
            
            int countryid = -1;
            Optional<Integer> optcountryid = this.getCountryID(customer.getCustCountry());
            if(! optcountryid.isPresent()){
                countryid = this.addCountry(customer.getCustCountry());
                System.out.println("Adding Country...");
            }
            else{
                countryid = optcountryid.get();
            }
            System.out.println("countryid: " + countryid);
    
            int cityid = -1;
            Optional<Integer> optcityid = this.getCityID(customer.getCustCity());
            if(! optcityid.isPresent() && countryid != -1){
                cityid = this.addCity(customer.getCustCity(), countryid);
                System.out.println("Adding City...");
            }
            else{
                cityid = optcityid.get();
            }
            System.out.println("cityid: " + cityid);
            
            int addressid = -1;
            Optional<Integer> optaddressid = this.getAddressID(customer.getCustAddress());
            if(! optaddressid.isPresent() && cityid != -1){
                System.out.println("Adding Address...");
                addressid = this.addAddress(    customer.getCustAddress(), 
                                                customer.getCustAddress2(), 
                                                cityid, 
                                                customer.getCustPostalCode(), 
                                                customer.getCustPhone());
                
            }
            else{
                System.out.println("Getting AddressID... " + optaddressid);
                addressid = optaddressid.get();
            }
            System.out.println("addressid: " + addressid);
            
            if(addressid != -1 && ! this.getCustomerID(customer.getCustName()).isPresent()){
                try{
                    this.dbConn.setAutoCommit(false);
                    String sqlAddCustomer =     "INSERT INTO customer (  customerName,\n" +
                                                "                       addressId,\n" +
                                                "                       active,\n" +
                                                "                       createDate,\n" +
                                                "                       createdBy,\n" +
                                                "                       lastUpdate,\n" +
                                                "                       lastUpdateBy)\n" +
                                                "VALUES (?, ?, ?, ?, ?, ?, ?);";

                    stmtAddCustomer = this.dbConn.prepareStatement(sqlAddCustomer);
                    stmtAddCustomer.setString(1, customer.getCustName());
                    stmtAddCustomer.setInt(2, addressid);
                    stmtAddCustomer.setBoolean(3, customer.getIsActive());
                    stmtAddCustomer.setTimestamp(4, Timestamp.from(ZonedDateTime.now().toInstant()));
                    stmtAddCustomer.setString(5, this.loggedInUser);
                    stmtAddCustomer.setTimestamp(6, Timestamp.from(ZonedDateTime.now().toInstant()));
                    stmtAddCustomer.setString(7, this.loggedInUser);

                    stmtAddCustomer.execute();
                }
                catch(SQLException ex){
                    if (this.dbConn != null) {
                        try {

                            this.dbConn.rollback();

                            System.out.println("Rolled back.");
                            ex.printStackTrace();
                        } catch (SQLException exrb) {
                            exrb.printStackTrace();
                            return addressid;
                        }
                        return addressid;
                    }
                }
                finally{
                    try{
                        if (stmtAddCustomer != null){
                            stmtAddCustomer.close();
                        }
                        this.dbConn.setAutoCommit(true);
                    }
                    catch (SQLException excs) {
                        excs.printStackTrace();
                    }

                    customerid = this.getCustomerID(customer.getCustName()).get().intValue();
                }
            }
            System.out.println("customerid: " + customerid);
            
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
        }
        finally{
            try{
                if (stmtAddCustomer != null){
                    stmtAddCustomer.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        return customerid;
    }
    
    public int addAppointment(Appointment appointment){
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return -1;
        }
        
        int appointmentid = -1;
        PreparedStatement stmtAddAppointment = null;
        try{
            this.dbConn.setAutoCommit(false);
            String sqlAddAppointment =      "INSERT INTO appointment (	customerId,\n" +
                                            "                           userId,\n" +
                                            "                           title,\n" +
                                            "                           description,\n" +
                                            "                           location,\n" +
                                            "                           contact,\n" +
                                            "                           type,\n" +
                                            "                           url,\n" +
                                            "                           start,\n" +
                                            "                           end,\n" +
                                            "                           createDate,\n" +
                                            "                           createdBy,\n" +
                                            "                           lastUpdate,\n" +
                                            "                           lastUpdateBy)\n" +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            
            stmtAddAppointment = this.dbConn.prepareStatement(sqlAddAppointment);
            stmtAddAppointment.setInt(1, appointment.getCustomer().getCustID());
            stmtAddAppointment.setInt(2, this.getLoggedInUserId());
            stmtAddAppointment.setString(3, appointment.getTitle());
            stmtAddAppointment.setString(4, appointment.getDescription());
            stmtAddAppointment.setString(5, appointment.getLocation());
            stmtAddAppointment.setString(6, "");
            stmtAddAppointment.setString(7, appointment.getType());
            stmtAddAppointment.setString(8, "");
            stmtAddAppointment.setTimestamp(9, Timestamp.from(appointment.getStart().toInstant()));
            stmtAddAppointment.setTimestamp(10, Timestamp.from(appointment.getEnd().toInstant()));
            stmtAddAppointment.setTimestamp(11, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddAppointment.setString(12, this.loggedInUser);
            stmtAddAppointment.setTimestamp(13, Timestamp.from(ZonedDateTime.now().toInstant()));
            stmtAddAppointment.setString(14, this.loggedInUser);
            
            stmtAddAppointment.execute();
        }
        catch(SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                    ex.printStackTrace();
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                    return appointmentid;
                }
                return appointmentid;
            }
        }
        finally{
            try{
                if (stmtAddAppointment != null){
                    stmtAddAppointment.close();
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
            
            appointmentid = this.getAppointmentID(appointment.getTitle()).get().intValue();
        }
        
        return appointmentid;
    }
    
    public void updateCountry(  int countryid,
                                String country){
        PreparedStatement updateCountryStatement = null;
        
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return;
        }
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlUpdateCountry =       "UPDATE	country\n" +
                                            "SET	country = ?,\n" +
                                            "           lastUpdate = ?,\n" +
                                            "           lastUpdateBy = ?\n" +
                                            "WHERE	countryId = ?";
            //System.out.println(sqlUpdateProjectUpdate);
            updateCountryStatement = this.dbConn.prepareStatement(sqlUpdateCountry);

            updateCountryStatement.setString(1, country);
            updateCountryStatement.setTimestamp(3, Timestamp.from(ZonedDateTime.now().toInstant()));
            updateCountryStatement.setString(4, this.loggedInUser);
            updateCountryStatement.setInt(5, countryid);
            
            
            int row = updateCountryStatement.executeUpdate();
            //System.out.println(updateProjectUpdateStatement.toString());
            //System.out.println(row);
            this.dbConn.commit();
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
        }
        finally{
            try{
                if (updateCountryStatement != null){
                    updateCountryStatement.close();
                    System.out.println("UpdateStatement closed");
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        
        
    }
    
    public void updateCity( int cityid,
                            String city,
                            int countryid){
        PreparedStatement updateCityStatement = null;
        
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return;
        }
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlUpdateCity =          "UPDATE	city\n" +
                                            "SET	city = ?,\n" +
                                            "           countryId = ?,\n" +
                                            "           lastUpdate = ?,\n" +
                                            "           lastUpdateBy = ?\n" +
                                            "WHERE	cityId = ?";
            //System.out.println(sqlUpdateProjectUpdate);
            updateCityStatement = this.dbConn.prepareStatement(sqlUpdateCity);

            updateCityStatement.setString(1, city);
            updateCityStatement.setInt(2, countryid);
            updateCityStatement.setTimestamp(3, Timestamp.from(ZonedDateTime.now().toInstant()));
            updateCityStatement.setString(4, this.loggedInUser);
            updateCityStatement.setInt(5, cityid);
            
            
            int row = updateCityStatement.executeUpdate();
            //System.out.println(updateProjectUpdateStatement.toString());
            //System.out.println(row);
            this.dbConn.commit();
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
        }
        finally{
            try{
                if (updateCityStatement != null){
                    updateCityStatement.close();
                    System.out.println("UpdateStatement closed");
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        
        
    }
    
    public void updateAddress(  int addressid,
                                String address,
                                String address2,
                                int cityid,
                                String postalcode,
                                String phone){
        PreparedStatement updateAddressStatement = null;
        
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return;
        }
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlUpdateAddress =      "UPDATE	address\n" +
                                            "SET	address = ?,\n" +
                                            "           address2 = ?,\n" +
                                            "           cityId = ?,\n" +
                                            "           postalCode = ?,\n" +
                                            "           phone = ?,\n" +
                                            "           lastUpdate = ?,\n" +
                                            "           lastUpdateBy = ?\n" +
                                            "WHERE	addressId = ?";
            //System.out.println(sqlUpdateProjectUpdate);
            updateAddressStatement = this.dbConn.prepareStatement(sqlUpdateAddress);

            updateAddressStatement.setString(1, address);
            updateAddressStatement.setString(2, address2);
            updateAddressStatement.setInt(3, cityid);
            updateAddressStatement.setString(4, postalcode);
            updateAddressStatement.setString(5, phone);
            updateAddressStatement.setTimestamp(6, Timestamp.from(ZonedDateTime.now().toInstant()));
            updateAddressStatement.setString(7, this.loggedInUser);
            updateAddressStatement.setInt(8, addressid);
            
            
            int row = updateAddressStatement.executeUpdate();
            //System.out.println(updateProjectUpdateStatement.toString());
            //System.out.println(row);
            this.dbConn.commit();
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
        }
        finally{
            try{
                if (updateAddressStatement != null){
                    updateAddressStatement.close();
                    System.out.println("UpdateStatement closed");
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        
        
    }
    
    public void updateCustomer(Customer oldcustomer, Customer customer){
        PreparedStatement updateCustomerStatement = null;
        
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return;
        }
        
        try{
            
            
            int countryid = -1;
            Optional<Integer> optcountryid = this.getCountryID(oldcustomer.getCustCountry());
            if(! optcountryid.isPresent()){
                countryid = this.addCountry(customer.getCustCountry());
                System.out.println("Adding Country...");
            }
            else{
                System.out.println("Updating City...");
                countryid = optcountryid.get();
                this.updateCountry(countryid, customer.getCustCountry());
            }
            System.out.println("countryid: " + countryid);
    
            int cityid = -1;
            Optional<Integer> optcityid = this.getCityID(oldcustomer.getCustCity());
            if(! optcityid.isPresent() && countryid != -1){
                cityid = this.addCity(customer.getCustCity(), countryid);
                System.out.println("Adding City...");
            }
            else{
                System.out.println("Updating City...");
                cityid = optcityid.get();
                this.updateCity(    cityid,
                                    customer.getCustCity(),
                                    countryid);
            }
            System.out.println("cityid: " + cityid);
            
            int addressid = -1;
            Optional<Integer> optaddressid = this.getAddressID(oldcustomer.getCustAddress());
            if(! optaddressid.isPresent() && cityid != -1){
                System.out.println("Adding Address...");
                addressid = this.addAddress(    customer.getCustAddress(), 
                                                customer.getCustAddress2(), 
                                                cityid, 
                                                customer.getCustPostalCode(), 
                                                customer.getCustPhone());
                
            }
            else{
                System.out.println("Updating City...");
                addressid = optaddressid.get();
                this.updateAddress( addressid, 
                                    customer.getCustAddress(), 
                                    customer.getCustAddress2(), 
                                    cityid, 
                                    customer.getCustPostalCode(), 
                                    customer.getCustPhone());
                                    
            }
            System.out.println("addressid: " + addressid);
            
            this.dbConn.setAutoCommit(false);
            
            String sqlUpdateCustomer =      "UPDATE	customer\n" +
                                            "SET	customerName = ?,\n" +
                                            "           addressId = ?,\n" +
                                            "           active = ?,\n" +
                                            "           lastUpdate = ?,\n" +
                                            "           lastUpdateBy = ?\n" +
                                            "WHERE	customerId = ?";
            //System.out.println(sqlUpdateProjectUpdate);
            updateCustomerStatement = this.dbConn.prepareStatement(sqlUpdateCustomer);

            updateCustomerStatement.setString(1, customer.getCustName());
            updateCustomerStatement.setInt(2, addressid);
            updateCustomerStatement.setBoolean(3, customer.getIsActive());
            updateCustomerStatement.setTimestamp(4, Timestamp.from(ZonedDateTime.now().toInstant()));
            updateCustomerStatement.setString(5, this.loggedInUser);
            updateCustomerStatement.setInt(6, customer.getCustID());
            
            System.out.println("Executing Customer Update...");
            int row = updateCustomerStatement.executeUpdate();
            System.out.println("Committing Customer Updated...");
            //System.out.println(updateProjectUpdateStatement.toString());
            //System.out.println(row);
            this.dbConn.commit();
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
        }
        finally{
            try{
                if (updateCustomerStatement != null){
                    updateCustomerStatement.close();
                    System.out.println("UpdateStatement closed");
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        
        
    }
    
    public void updateAppointment(Appointment oldappointment, Appointment appointment){
        PreparedStatement updateAppointmentStatement = null;
        
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return;
        }
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlUpdateAppointment =   "UPDATE	appointment\n" +
                                            "SET	customerId = ?,\n" +
                                            "           userId = ?,\n" +
                                            "           title = ?,\n" +
                                            "           description = ?,\n" +
                                            "           location = ?,\n" +
                                            "           type = ?,\n" +
                                            "           start = ?,\n" +
                                            "           end = ?,\n" +
                                            "           lastUpdate = ?,\n" +
                                            "           lastUpdateBy = ?\n" +
                                            "WHERE	appointmentId = ?";
            //System.out.println(sqlUpdateProjectUpdate);
            updateAppointmentStatement = this.dbConn.prepareStatement(sqlUpdateAppointment);

            updateAppointmentStatement.setInt(1, appointment.getCustomer().getCustID());
            updateAppointmentStatement.setInt(2, this.loggedInUserId);
            updateAppointmentStatement.setString(3, appointment.getTitle());
            updateAppointmentStatement.setString(4, appointment.getDescription());
            updateAppointmentStatement.setString(5, appointment.getLocation());
            updateAppointmentStatement.setString(6, appointment.getType());
            updateAppointmentStatement.setTimestamp(7, Timestamp.from(appointment.getStart().toInstant()));
            updateAppointmentStatement.setTimestamp(8, Timestamp.from(appointment.getEnd().toInstant()));
            updateAppointmentStatement.setTimestamp(9, Timestamp.from(ZonedDateTime.now().toInstant()));
            updateAppointmentStatement.setString(10, this.loggedInUser);
            updateAppointmentStatement.setInt(11, oldappointment.getAppointmentId());
            
            System.out.println("Executing Customer Update...");
            int row = updateAppointmentStatement.executeUpdate();
            System.out.println("Committing Customer Updated...");
            //System.out.println(updateProjectUpdateStatement.toString());
            //System.out.println(row);
            this.dbConn.commit();
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
            ex.printStackTrace();
        }
        finally{
            try{
                if (updateAppointmentStatement != null){
                    updateAppointmentStatement.close();
                    System.out.println("UpdateStatement closed");
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        
        
    }
    
    public void deleteCustomer(Customer customer){
        PreparedStatement deleteCustomerStatement = null;
        
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return;
        }
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlDeleteCustomer =      "DELETE FROM customer\n" +
                                            "WHERE	customerId = ?";
            //System.out.println(sqlUpdateProjectUpdate);
            deleteCustomerStatement = this.dbConn.prepareStatement(sqlDeleteCustomer);

            deleteCustomerStatement.setInt(1, customer.getCustID());
            
            System.out.println("Executing Customer Delete...");
            deleteCustomerStatement.execute();
            System.out.println("Committing Customer Delete...");
            //System.out.println(updateProjectUpdateStatement.toString());
            //System.out.println(row);
            this.dbConn.commit();
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
            ex.printStackTrace();
        }
        finally{
            try{
                if (deleteCustomerStatement != null){
                    deleteCustomerStatement.close();
                    System.out.println("Delete Statement closed");
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        
        
    }
    
    public void deleteAppointment(Appointment appointment){
        PreparedStatement deleteAppointmentStatement = null;
        
        
        
        if(!this.isConnected()){
            System.out.println("DB Connection has not been made.  Connect and try again.");
            return;
        }
        
        try{
            this.dbConn.setAutoCommit(false);
            
            String sqlDeleteAppointment =   "DELETE FROM appointment\n" +
                                            "WHERE	appointmentId = ?";
            //System.out.println(sqlUpdateProjectUpdate);
            deleteAppointmentStatement = this.dbConn.prepareStatement(sqlDeleteAppointment);

            deleteAppointmentStatement.setInt(1, appointment.getAppointmentId());
            
            System.out.println("Executing Customer Delete...");
            deleteAppointmentStatement.execute();
            System.out.println("Committing Customer Delete...");
            //System.out.println(updateProjectUpdateStatement.toString());
            //System.out.println(row);
            this.dbConn.commit();
            
            
        }
        catch (SQLException ex){
            if (this.dbConn != null) {
                try {
 
                    this.dbConn.rollback();
 
                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
            ex.printStackTrace();
        }
        finally{
            try{
                if (deleteAppointmentStatement != null){
                    deleteAppointmentStatement.close();
                    System.out.println("Delete Statement closed");
                }
                this.dbConn.setAutoCommit(true);
            }
            catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
        
        
    }

    

}
