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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
    
    @FXML private TextField txtCustomer;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtLocation;
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMinute;
    @FXML private TextField txtDuration;
    
    @FXML private DatePicker dtpkCalendarBegin;
    @FXML private DatePicker dtpkCalendarEnd;
    @FXML private DatePicker dtpkStart;
    
    @FXML private Button btnEditCustomers;
    @FXML private Button btnLogout;

    HomePageController(){
        
    }
    HomePageController(Stage appstage, DatabaseController dbCtrl) {
        this.appStage = appstage;
        this.dbCtrl = dbCtrl;
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
            //Locale locale = new Locale("es", "MX");
            Locale locale = Locale.getDefault();
            ResourceBundle bundle = ResourceBundle.getBundle("resources/custmanagement", locale);
            FXMLLoader loginpageloader = new FXMLLoader(getClass().getResource("/customermanagement/Views/LoginPage.fxml"), bundle);
            //System.out.println(getClass().getResource("Views/HomePage.fxml"));  !!! RETURNS NULL !!!
            System.out.println("HomePageLogoutAppStage: " + this.appStage);
            LoginPageController loginpagecontroller = new LoginPageController(this.appStage, this.dbCtrl);
            loginpageloader.setController(loginpagecontroller);

            Parent root = loginpageloader.load();
            Scene scene = new Scene(root);
            this.appStage.setScene(scene);
            //this.appStage.setResizable(true);
            this.appStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
        
        //Stage stage = (Stage) this.btnLogout.getScene().getWindow();
        //stage.close();
    }
    
    @FXML
    private void handleDateBack(ActionEvent event){
        String calperiod = (String) this.choiceCalendarView.getValue();
        
        LocalDate beginning = this.dtpkCalendarBegin.getValue();
        LocalDate ending = this.dtpkCalendarEnd.getValue();
        LocalDate newbeginning = beginning;
        LocalDate newending = ending;
        
        if(calperiod.equals("Week")){
            newbeginning = beginning.plusDays(-7);
            newending = ending.plusDays(-7);
        }
        if(calperiod.equals("Month")){
            newbeginning = beginning.plusMonths(-1).withDayOfMonth(1);
            newending = beginning.plusMonths(-1).withDayOfMonth(newbeginning.lengthOfMonth());
        }
        
        this.dtpkCalendarBegin.setValue(newbeginning);
        this.dtpkCalendarEnd.setValue(newending);
        
        ArrayList<Appointment> appointments;
        appointments = this.dbCtrl.getAppointments(newbeginning.atStartOfDay(), newending.atTime(LocalTime.MAX));
        
        this.lstAppointments = FXCollections.observableArrayList(appointments);
        this.tblAppointments.setItems(this.lstAppointments);
    }
    
    @FXML
    private void handleDateForward(ActionEvent event){
        String calperiod = (String) this.choiceCalendarView.getValue();
        
        LocalDate beginning = this.dtpkCalendarBegin.getValue();
        LocalDate ending = this.dtpkCalendarEnd.getValue();
        LocalDate newbeginning = beginning;
        LocalDate newending = ending;
        
        if(calperiod.equals("Week")){
            newbeginning = beginning.plusDays(7);
            newending = ending.plusDays(7);
        }
        if(calperiod.equals("Month")){
            newbeginning = beginning.plusMonths(1).withDayOfMonth(1);
            newending = beginning.plusMonths(1).withDayOfMonth(newbeginning.lengthOfMonth());
        }
        
        this.dtpkCalendarBegin.setValue(newbeginning);
        this.dtpkCalendarEnd.setValue(newending);
        
        ArrayList<Appointment> appointments;
        appointments = this.dbCtrl.getAppointments(newbeginning.atStartOfDay(), newending.atTime(LocalTime.MAX));
        
        this.lstAppointments = FXCollections.observableArrayList(appointments);
        this.tblAppointments.setItems(this.lstAppointments);
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
        this.dtpkStart.setValue(null);
        
        this.editMode = "new";
    }
    
    @FXML
    private void handleNew(ActionEvent event) {
        // Set lstCustomers.Selection to NULL
        this.tblAppointments.getSelectionModel().clearSelection();
        
        this.selectedCustomer = null;
        this.txtCustomer.setText("");
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

            CustomerEditController customereditcontroller = new CustomerEditController("select", this.selectedCustomer, this.txtCustomer, dbCtrl);
            customereditloader.setController(customereditcontroller);

            Parent root = customereditloader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            //this.appStage.setResizable(true);
            stage.show();
            this.txtCustomer.setText(this.selectedCustomer.getCustName());
        } catch (IOException e){
            e.printStackTrace();
        }
        
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        try{
            LocalTime starttime = LocalTime.of( Integer.parseInt(this.txtStartHour.getText()), 
                                                Integer.parseInt(this.txtStartMinute.getText()));
            Duration duration = Duration.ofMinutes(Integer.parseInt(this.txtDuration.getText()));
            LocalTime endtime = starttime.plus(duration);
            ZonedDateTime startdatetime = ZonedDateTime.of(this.dtpkStart.getValue(), starttime, ZoneId.systemDefault());
            ZonedDateTime enddatetime = startdatetime.plus(duration);
            String type = (String) this.choiceType.getValue();

            if(endtime.compareTo(LocalTime.of(17, 00)) > 0){
                throw new RuntimeException();
            }
            if(starttime.compareTo(LocalTime.of(8, 00)) < 0){
                throw new RuntimeException();
            }
            System.out.println("Saving to appointment customer: " + this.selectedCustomer.getCustName());

            Appointment newappointment = new Appointment(   this.selectedCustomer,
                                                                this.dbCtrl.getLoggedInUserId(),
                                                                this.txtTitle.getText(),
                                                                this.txtDescription.getText(),
                                                                this.txtLocation.getText(),
                                                                type,
                                                                startdatetime,
                                                                enddatetime);
            try{
                ArrayList<Appointment> overlappingapts;
                overlappingapts = this.dbCtrl.getOverlappingAppointments(   startdatetime.toLocalDateTime(), 
                                                                            enddatetime.toLocalDateTime());
                if(! overlappingapts.isEmpty()){
                    throw new RuntimeException();
                }
                
                if(this.editMode.equals("new")){
                    int aptid = this.dbCtrl.addAppointment(newappointment);
                    if(aptid != -1){
                        newappointment.setAppointmentId(aptid);
                        this.lstAppointments.add(newappointment);
                    }
                    else{
                        System.out.println("AddAppointment Failed");
                    }
                }
                else if (this.editMode.equals("update")){
                    newappointment.setAppointmentId(this.activeAppointment.getAppointmentId());
                    this.dbCtrl.updateAppointment(this.activeAppointment, newappointment);
                    this.lstAppointments.set(this.activeAppointmentIndex, newappointment);
                    this.activeAppointment = newappointment;
                }
            }
            catch(RuntimeException ex){
                Alert overlappingapt = new Alert(AlertType.ERROR, "Can not schedule an overlapping appointment.  Please adjust your schedule.");
                overlappingapt.show();
            }

            
        }
        catch(RuntimeException e){
            Alert outsidebusiness = new Alert(AlertType.ERROR, "Can not schedule appointment outside of business hours 8AM-5PM.");
            outsidebusiness.show();
        }
        
        
    }
    
    @FXML
    private void handleRunReport(ActionEvent event) {
        String reporttype = (String) this.choiceReport.getValue();
        System.out.println(reporttype);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String reporttime = LocalDateTime.now().format(formatter);
        if(reporttype != null){
            if(reporttype.equals("Appointment Types by Month")){
                this.dbCtrl.generateReport( this.dbCtrl.retrieveAppointmentTypesByMonth(), 
                                            "AppointmentTypesByMonth_" + reporttime + ".csv");
            }
            else if (reporttype.equals("Consultant Schedules")){
                this.dbCtrl.generateReport( this.dbCtrl.retrieveConsultantSchedules(), 
                                            "ConsultantSchedules_" + reporttime + ".csv");
            }
            else if (reporttype.equals("Appointment Types by Customer")){
                this.dbCtrl.generateReport( this.dbCtrl.retrieveAppointmentTypesByCustomer(), 
                                            "AppointmentTypesByCustomer_" + reporttime + ".csv");
            }
            else{
                Alert invalidreportalert = new Alert(AlertType.ERROR, "Please select a valid report.");
                invalidreportalert.show();
            }
        }
        else{
            Alert invalidreportalert = new Alert(AlertType.ERROR, "Please select a valid report.");
            invalidreportalert.show();
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
        dtpkCalendarBegin.setStyle("-fx-opacity: 1");
        dtpkCalendarBegin.getEditor().setStyle("-fx-opacity: 1");
        dtpkCalendarEnd.setStyle("-fx-opacity: 1");
        dtpkCalendarEnd.getEditor().setStyle("-fx-opacity: 1");
        
        // Set User Name
        this.lblUserName.setText(this.dbCtrl.getLoggedInUser());
        // Default edit-mode to "new"
        this.editMode = "new";
        
        //
        this.selectedCustomer = new Customer();
        
        // Populate Calendar View options (Week/Month)
        // Then set on change lambda to update the appointment table
        /*
        
        ########################################################################
        ------------------------- LAMBDA 1 -------------------------------------
        ########################################################################
            When you switch calendar views between month and week, the calendar
        appointments need to 1. choose a start date for the week/month and 2.
        update the appointments list to reflect the whole time period.
        */
        ObservableList<String> views = FXCollections.observableArrayList();
        views.addAll("Week", "Month");
        this.choiceCalendarView.setItems(views);
        this.choiceCalendarView.getSelectionModel()
        .selectedItemProperty()
        .addListener( (obs, oldV, newV) -> {
            LocalDate beginning = LocalDate.now();
            LocalDate ending = LocalDate.now();
            if(newV.equals("Month")){
                beginning = beginning.withDayOfMonth(1);
                ending = ending.withDayOfMonth(beginning.lengthOfMonth());
            }
            else{
                TemporalField beginweek = WeekFields.of(Locale.getDefault()).dayOfWeek();
                beginning = beginning.with(beginweek, 1);
                ending = beginning.plusDays(6);
            }
            ArrayList<Appointment> appointments;
            appointments = this.dbCtrl.getAppointments(beginning.atStartOfDay(), ending.atTime(LocalTime.MAX));
            this.lstAppointments = FXCollections.observableArrayList(appointments);
            this.tblAppointments.setItems(this.lstAppointments);
            this.dtpkCalendarBegin.setValue(beginning);
            this.dtpkCalendarEnd.setValue(ending);
        });
        // Set selection default to "Week", calling lambda above and populating
        // appointments
        this.choiceCalendarView.getSelectionModel().select(0);

        // Populate Report Option List
        ObservableList<String> reports = FXCollections.observableArrayList();
        reports.addAll("Appointment Types by Month", "Consultant Schedules", "Appointment Types by Customer");
        this.choiceReport.setItems(reports);
        
        // Populate Appointment Type List
        ObservableList<String> types = FXCollections.observableArrayList();
        types.addAll("Scrum", "Presentation");
        this.choiceType.setItems(types);
        
        
        /*
        
        ########################################################################
        ------------------------- LAMBDA 2 -------------------------------------
        ########################################################################
            When you switch select an appointment, the appointment interface
        needs to reflect the appropriate information for viewing and editing
        purposes.
        */
        this.tblAppointments.setOnMousePressed(e ->{
            if (e.getClickCount() == 1 && e.isPrimaryButtonDown() ){
                Appointment appointment = this.tblAppointments.getSelectionModel().getSelectedItem();
                int index = this.tblAppointments.getSelectionModel().getSelectedIndex();
                this.activeAppointment = appointment;
                this.activeAppointmentIndex = index;

                this.editMode = "update";

                this.selectedCustomer = this.activeAppointment.getCustomer();
                this.txtCustomer.setText(this.selectedCustomer.getCustName());
                this.txtTitle.setText(this.activeAppointment.getTitle());
                this.txtDescription.setText(this.activeAppointment.getDescription());
                this.txtLocation.setText(this.activeAppointment.getLocation());
                this.choiceType.setValue(this.activeAppointment.getType());
                this.txtStartHour.setText(String.valueOf(this.activeAppointment.getStart().getHour()));
                this.txtStartMinute.setText(String.valueOf(this.activeAppointment.getStart().getMinute()));
                this.txtDuration.setText(String.valueOf(ChronoUnit.MINUTES.between(this.activeAppointment.getStart(), this.activeAppointment.getEnd())));
                this.dtpkStart.setValue(this.activeAppointment.getStart().toLocalDate());
            }
        }); 
        
        // TODO
        System.out.println("COMPLETED HOMEPAGE INITIALIZATION");
        
    
    }
}
