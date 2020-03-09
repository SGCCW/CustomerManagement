/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customermanagement.Controllers;

import customermanagement.CustomerManagement;
import customermanagement.Database.DatabaseController;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author Collin
 */
public class LoginPageController implements Initializable {
    private DatabaseController dbCtrl = null;
    private ResourceBundle rb;
    private Stage appStage;
    private boolean autoLogin = false;
    private String autoUserName = "";
    private String autoPassword = "";
    

    @FXML private TextField txtUserName;
    @FXML private PasswordField passUserPass;
    
    @FXML private CheckBox chkRememberMe;
    @FXML private Button btnLogin;
    
    /**
     * Constructs the controller class.
     */
    public LoginPageController(Stage stage, DatabaseController dbctrl){
        this.appStage = stage;
        this.dbCtrl = dbctrl;
        this.autoUserName = "";
        this.autoPassword = "";
        System.out.println(this.appStage);
    }
    
    public LoginPageController(Stage stage, DatabaseController dbctrl, String username, String password){
        System.out.println("Controller First");
        this.appStage = stage;
        this.dbCtrl = dbctrl;
        this.autoLogin = true;
        this.autoUserName = username;
        this.autoPassword = password;
        System.out.println(this.appStage);
    }
    
    @FXML
    private void handleLogin() {
        System.out.println("Login!");
        
        String username;
        String userpass;
        if(! this.autoUserName.equals("")){
            username = this.autoUserName;
            userpass = this.autoPassword;
        }
        else{
            username = txtUserName.getText();
            userpass = passUserPass.getText();
        }
        System.out.println("username: " + username);
        System.out.println("userpass: " + userpass);
        // Username required
        if(username.equals("")){
            Alert alertuserreq = new Alert(AlertType.ERROR, this.rb.getString("userreq"));
            alertuserreq.show();
            return;
        }
        
        // If chkRememberMe.isSelected() == true
        // && ! credentials saved... save to file.
        Path path = Paths.get("src/resources/credstr");
        if(this.chkRememberMe.isSelected() && ! Files.exists(path)){
            String creds = username + "\n" + userpass + "\n";
            try {
                Files.write(path, creds.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(LoginPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // If chkRememberMe.isSelected() == false
        // && credentials saved... delete file
        if(! this.chkRememberMe.isSelected() && Files.exists(path)){
            try {
                Files.delete(path);
            } catch (IOException ex) {
                Logger.getLogger(LoginPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try{
            int success = this.dbCtrl.authenticateUser(username, userpass);
            if(success == -1){
                throw new RuntimeException("User authentication failed.");
            }
            
            // Track Login
            Path loginpath = Paths.get("src/resources/log-ins.txt");
            if(Files.exists(loginpath)){
                try{
                    BufferedWriter writer = new BufferedWriter(
                                                new FileWriter("src/resources/log-ins.txt", true)  //Set true for append mode
                                            );  
                    writer.newLine();   //Add new line
                    writer.write(username + ", " + LocalDateTime.now().toString());
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                try (   FileWriter writer = new FileWriter("src/resources/log-ins.txt");
                        BufferedWriter bw = new BufferedWriter(writer)) {

                       bw.write(username + ", " + LocalDateTime.now().toString());

               } catch (IOException e) {
                       System.err.format("IOException: %s%n", e);
               }
            }
            
            // Enter program
            try{
                this.dbCtrl.setLoggedInUser(username);
                // Alert user if appointment exists within 15 minutes of login
                ZonedDateTime nearestappointmentstart = this.dbCtrl.getNearestAppointmentDateTime();
                if(     nearestappointmentstart != null &&
                        nearestappointmentstart.compareTo(ZonedDateTime.now().plusMinutes(15)) <= 0
                    ){
                    Alert alertappointmentwarning = new Alert(AlertType.WARNING, "You have an appointment in less than 15 minutes.");
                    alertappointmentwarning.show();
                }

                FXMLLoader homeloader = new FXMLLoader(getClass().getResource("/customermanagement/Views/HomePage.fxml"));
                //System.out.println(getClass().getResource("Views/HomePage.fxml"));  !!! RETURNS NULL !!!

                HomePageController homecontroller = new HomePageController(this.appStage, dbCtrl);
                homeloader.setController(homecontroller);

                Parent root = homeloader.load();
                Scene scene = new Scene(root);

                this.appStage.setScene(scene);
                this.appStage.show();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        catch(RuntimeException e){
            e.printStackTrace();
            Alert alertauthfailed = new Alert(AlertType.ERROR, this.rb.getString("userauthfail"));
            alertauthfailed.show();
        }
        
        /*
        if(success != -1){
            // Track Login
            Path loginpath = Paths.get("src/resources/log-ins.txt");
            if(Files.exists(loginpath)){
                try{
                    BufferedWriter writer = new BufferedWriter(
                                                new FileWriter("src/resources/log-ins.txt", true)  //Set true for append mode
                                            );  
                    writer.newLine();   //Add new line
                    writer.write(username + ", " + LocalDateTime.now().toString());
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                try (   FileWriter writer = new FileWriter("src/resources/log-ins.txt");
                        BufferedWriter bw = new BufferedWriter(writer)) {

                       bw.write(username + ", " + LocalDateTime.now().toString());

               } catch (IOException e) {
                       System.err.format("IOException: %s%n", e);
               }
            }
            // Enter program
            try{
                this.dbCtrl.setLoggedInUser(username);

                FXMLLoader homeloader = new FXMLLoader(getClass().getResource("/customermanagement/Views/HomePage.fxml"));
                //System.out.println(getClass().getResource("Views/HomePage.fxml"));  !!! RETURNS NULL !!!

                HomePageController homecontroller = new HomePageController(dbCtrl);
                homeloader.setController(homecontroller);

                Parent root = homeloader.load();
                Scene scene = new Scene(root);

                this.appStage.setScene(scene);
                //this.appStage.setResizable(true);
                this.appStage.show();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        else{
            //Error failed to auth;
        }*/
        
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.btnLogin.setOnAction(event -> handleLogin());
        if(this.autoLogin == true){
            this.txtUserName.setText(this.autoUserName);
            this.passUserPass.setText(this.autoPassword);


            this.chkRememberMe.setSelected(true);
        }
        this.rb = rb;
    }    
    
}
