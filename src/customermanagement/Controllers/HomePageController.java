/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customermanagement.Controllers;

import customermanagement.CustomerManagement;
import customermanagement.Database.DatabaseController;
import customermanagement.Models.Appointment;
import customermanagement.Models.Customer;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Collin
 */
public class HomePageController implements Initializable {
    private DatabaseController dbCtrl = null;
    private Stage appStage;
    private Appointment activeAppointment;
    private int activeAppointmentIndex;
    private ObservableList<Appointment> lstAppointments;
    private Customer selectedCustomer;
    private String editMode;
    
    @FXML private Label lblUserName;
    
    @FXML private ChoiceBox choiceCalendarView;
    @FXML private ChoiceBox choiceReport;
    @FXML private ChoiceBox choiceType;
    
    @FXML private TableView<Appointment> tblAppointments;
    
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtLocation;
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMinute;
    @FXML private TextField txtDuration;
    
    @FXML private DatePicker dtpkStart;
    
    @FXML private Button btnEditCustomers;
    @FXML private Button btnLogout;

    HomePageController(){
        
    }
    HomePageController(DatabaseController dbCtrl) {
        this.dbCtrl = dbCtrl;
    }
    
    @FXML
    private void handleDelete(ActionEvent event) {
        this.dbCtrl.deleteAppointment(this.activeAppointment);
        this.lstAppointments.remove(this.activeAppointment);
        this.tblAppointments.getSelectionModel().clearSelection();
        this.txtTitle.setText("");
        this.txtDescription.setText("");
        this.txtLocation.setText("");
        this.choiceType.setValue(null);
        this.txtStartHour.setText("");
        this.txtStartMinute.setText("");
        this.txtDuration.setText("");
        
        this.editMode = "new";
    }
    
    @FXML
    private void handleLogout(ActionEvent event){
        System.out.println("LOGGING OUT!");
        try{
            Path path = Paths.get("src/resources/credstr");
            if(Files.exists(path)){
                Files.delete(path);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        try{
            FXMLLoader loginpageloader = new FXMLLoader(getClass().getResource("/customermanagement/Views/LoginPage.fxml"));
            //System.out.println(getClass().getResource("Views/HomePage.fxml"));  !!! RETURNS NULL !!!

            LoginPageController loginpagecontroller = new LoginPageController(this.appStage, this.dbCtrl);
            loginpageloader.setController(loginpagecontroller);

            Parent root = loginpageloader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            //this.appStage.setResizable(true);
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
        
        Stage stage = (Stage) this.btnLogout.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleNew(ActionEvent event) {
        // Set lstCustomers.Selection to NULL
        this.tblAppointments.getSelectionModel().clearSelection();
        
        this.txtTitle.setText("");
        this.txtDescription.setText("");
        this.txtLocation.setText("");
        this.choiceType.setValue(null);
        this.txtStartHour.setText("");
        this.txtStartMinute.setText("");
        this.txtDuration.setText("");
        this.dtpkStart.setValue(null);
        // Set this.editMode = 'new'
        this.editMode = "new";
        
    }
    
    @FXML
    private void handleSelectCustomer(ActionEvent event) {
        System.out.println("SelectedCustomer's name: " + this.selectedCustomer.getCustName());
        try{
            FXMLLoader customereditloader = new FXMLLoader(getClass().getResource("/customermanagement/Views/CustomerEditPage.fxml"));
            //System.out.println(getClass().getResource("Views/HomePage.fxml"));  !!! RETURNS NULL !!!

            CustomerEditController customereditcontroller = new CustomerEditController("select", this.selectedCustomer, dbCtrl);
            customereditloader.setController(customereditcontroller);

            Parent root = customereditloader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            //this.appStage.setResizable(true);
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
        
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        LocalTime starttime = LocalTime.of( Integer.parseInt(this.txtStartHour.getText()), 
                                            Integer.parseInt(this.txtStartMinute.getText()));
        Duration duration = Duration.ofMinutes(Integer.parseInt(this.txtDuration.getText()));
        LocalTime endtime = starttime.plus(duration);
        LocalDateTime startdatetime = LocalDateTime.of(this.dtpkStart.getValue(), starttime);
        LocalDateTime enddatetime = startdatetime.plus(duration);
        String type = (String) this.choiceType.getValue();
        System.out.println("Saving to appointment customer: " + this.selectedCustomer.getCustName());
        System.out.println(this.dbCtrl.getLoggedInUserId());
        System.out.println(this.txtTitle.getText());
        System.out.println(this.txtDescription.getText());
        System.out.println(this.txtLocation.getText());
        System.out.println((String) this.choiceType.getValue());
        System.out.println(startdatetime);
        System.out.println(enddatetime);
        
        
        Appointment newappointment = new Appointment(   this.selectedCustomer,
                                                        this.dbCtrl.getLoggedInUserId(),
                                                        this.txtTitle.getText(),
                                                        this.txtDescription.getText(),
                                                        this.txtLocation.getText(),
                                                        type,
                                                        startdatetime,
                                                        enddatetime);
        
        int aptid = this.dbCtrl.addAppointment(newappointment);
        if(aptid != -1){
            newappointment.setAppointmentId(aptid);
            this.lstAppointments.add(newappointment);
        }
        else{
            System.out.println("AddAppointment Failed");
        }
    }
    
    @FXML
    private void handleRunReport(ActionEvent event) {
        String reporttype = (String) this.choiceReport.getValue();
        if(reporttype.equals("Appointment Types by Month")){
            
        }
        else if (reporttype.equals("Consultant Schedules")){
            
        }
        else if (reporttype.equals("Appointment Types by Customer")){
            
        }
    }
    
    @FXML
    private void handleEditCustomers(ActionEvent event) {
        try{
            FXMLLoader customereditloader = new FXMLLoader(getClass().getResource("/customermanagement/Views/CustomerEditPage.fxml"));
            //System.out.println(getClass().getResource("Views/HomePage.fxml"));  !!! RETURNS NULL !!!

            CustomerEditController customereditcontroller = new CustomerEditController(dbCtrl);
            customereditloader.setController(customereditcontroller);

            Parent root = customereditloader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            //this.appStage.setResizable(true);
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set User Name
        this.lblUserName.setText(this.dbCtrl.getLoggedInUser());
        // Default edit-mode to "new"
        this.editMode = "new";
        
        //
        this.selectedCustomer = new Customer();
        
        // Populate Calendar View options (Week/Month)
        // Then set on change lambda to update the appointment table
        ObservableList<String> views = FXCollections.observableArrayList();
        views.addAll("Week", "Month");
        this.choiceCalendarView.setItems(views);
        this.choiceCalendarView.getSelectionModel()
        .selectedItemProperty()
        .addListener( (obs, oldV, newV) -> System.out.println(newV) );
        this.choiceCalendarView.getSelectionModel().select(0);

        // Populate Report Option List
        ObservableList<String> reports = FXCollections.observableArrayList();
        reports.addAll("Appointment Types by Month", "Consultant Schedules", "Appointment Types by Customer");
        this.choiceReport.setItems(reports);
        
        // Populate Appointment Type List
        ObservableList<String> types = FXCollections.observableArrayList();
        types.addAll("Scrum", "Presentation");
        this.choiceType.setItems(types);
        
        // Populate Calendar Appointments
        String calview = (String) this.choiceCalendarView.getValue();
        ArrayList<Appointment> appointments;
        if(calview.equals("Week")){
            appointments = this.dbCtrl.getAppointments(LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        }
        else{
            appointments = this.dbCtrl.getAppointments(LocalDateTime.now(), LocalDateTime.now().plusDays(31));
        }
        
        this.lstAppointments = FXCollections.observableArrayList(appointments);
        this.tblAppointments.setItems(this.lstAppointments);
        
        this.tblAppointments.setOnMousePressed(e ->{
            if (e.getClickCount() == 1 && e.isPrimaryButtonDown() ){
                Appointment appointment = this.tblAppointments.getSelectionModel().getSelectedItem();
                int index = this.tblAppointments.getSelectionModel().getSelectedIndex();
                this.activeAppointment = appointment;
                this.activeAppointmentIndex = index;

                this.editMode = "update";

                this.txtTitle.setText(this.activeAppointment.getTitle());
                this.txtDescription.setText(this.activeAppointment.getDescription());
                this.txtLocation.setText(this.activeAppointment.getLocation());
                this.choiceType.setValue(this.activeAppointment.getType());
                //this.txtStartHour.setText(this.activeAppointment.get);
                //this.txtStartMinute.setText(this.activeAppointment.get);
                //this.txtDuration.setText(this.activeAppointment.get);
                //this.dtpkStart.setValue(null);
            }
        }); 
        
        // TODO
        System.out.println("COMPLETED HOMEPAGE INITIALIZATION");
        
    
    }
