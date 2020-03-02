/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customermanagement;

import customermanagement.Database.DatabaseController;
import customermanagement.Controllers.LoginPageController;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Collin
 */
public class CustomerManagement extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        DatabaseController dbctrl = new DatabaseController( "3.227.166.251",
                                                            "U05tuC",
                                                            "U05tuC",
                                                            "53688605902");
        
        stage.setOnCloseRequest(e->{
            Platform.exit();
            dbctrl.disconnect();
            System.exit(0);
        });
        
        //Prep LoginPageController
        LoginPageController loginpagecontroller;
        
        // Check to see if credentials saved.  If so, automatically login
        Path path = Paths.get("src/resources/credstr");
        System.out.println(Files.exists(path));
        if(Files.exists(path)){
            String username = "";
            String password = "";
            final List<String> lines = Files.readAllLines(path);
            int i = 0;
            for(String line : lines){
                line = line.replace("\n", "");
                if(i == 0) username = line;
                else if(i == 1) password = line;
                else break;
                i++;
            }
            loginpagecontroller = new LoginPageController(stage, dbctrl, username, password);
        }
        else{
            loginpagecontroller = new LoginPageController(stage, dbctrl);
        }
                
        FXMLLoader loginpageloader = new FXMLLoader(getClass().getResource("Views/LoginPage.fxml"));
        
            
        loginpageloader.setController(loginpagecontroller);
        Parent root = loginpageloader.load();
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
