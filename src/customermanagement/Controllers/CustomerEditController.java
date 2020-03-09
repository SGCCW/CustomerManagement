/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customermanagement.Controllers;

import customermanagement.Database.DatabaseController;
import customermanagement.Models.Customer;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author collin.walker
 */
public class CustomerEditController implements Initializable {
    
    private String editMode = "";
    private DatabaseController dbCtrl;
    private Customer selectedCustomer;
    private TextField customerDisplay;
    private Customer activeCustomer;
    private int activeCustomerIndex;
    private ObservableList<Customer> lstCustomers;
    
    @FXML private Button btnSearch;
    @FXML private Button btnNew;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;
    @FXML private Button btnSelect;
    
    @FXML private TextField txtSearch;
    @FXML private TextField txtName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtAddress2;
    @FXML private TextField txtCity;
    @FXML private TextField txtCountry;
    @FXML private TextField txtPostalCode;
    @FXML private TextField txtPhone;
    
    @FXML private CheckBox chkActive;
    
    @FXML private TableView<Customer> tblCustomers;

    CustomerEditController(DatabaseController dbCtrl) {
        this.dbCtrl = dbCtrl;
    }
    CustomerEditController(String editmode, Customer customer, TextField display, DatabaseController dbCtrl){
        this.dbCtrl = dbCtrl;
        this.editMode = editmode;
        this.selectedCustomer = customer;
        this.customerDisplay = display;
    }
    
    
    
    @FXML
    private void handleSearch(ActionEvent event) {
        // Retrieve database Customers WHERE name LIKE '%[txtSearch]%'
        //                             OR address LIKE '%[txtSearch]%'
        //                             OR phone LIKE '%[txtSearch]%'
        
        //Populate tblCustomers with database results
    }
    @FXML
    private void handleNew(ActionEvent event) {
        // Set lstCustomers.Selection to NULL
        this.tblCustomers.getSelectionModel().clearSelection();
        // Set txtName, txtAddress, and txtPhone .text = ""
        this.txtSearch.setText("");
        this.txtName.setText("");
        this.txtAddress.setText("");
        this.txtAddress2.setText("");
        this.txtCity.setText("");
        this.txtCountry.setText("");
        this.txtPostalCode.setText("");
        this.txtPhone.setText("");
        // Set this.editMode = 'new'
        this.editMode = "new";
    }
    @FXML
    private void handleDelete(ActionEvent event) {
        // Execute database delete statement on selected customer
        this.dbCtrl.deleteCustomer(this.activeCustomer);
        // Remove selected customer from list
        this.lstCustomers.remove(this.activeCustomer);
        // Set lstCustomers.Selection to NULL
        this.tblCustomers.getSelectionModel().clearSelection();
        // Clear data field text
        this.txtSearch.setText("");
        this.txtName.setText("");
        this.txtAddress.setText("");
        this.txtAddress2.setText("");
        this.txtCity.setText("");
        this.txtCountry.setText("");
        this.txtPostalCode.setText("");
        this.txtPhone.setText("");
        // Set this.editMode = 'new'
        this.editMode = "new";
    }
    @FXML 
    private void handleSave(ActionEvent event) {
        Customer savedcustomer = new Customer(  this.txtName.getText(),
                                                this.txtAddress.getText(),
                                                this.txtAddress2.getText(),
                                                this.txtCity.getText(),
                                                this.txtCountry.getText(),
                                                this.txtPostalCode.getText(),
                                                this.txtPhone.getText(),
                                                this.chkActive.isSelected());
        System.out.println("EditMode: " + this.editMode);
        if(this.editMode.equals("new")){
            try{
                int cid = this.dbCtrl.addCustomer(savedcustomer);
                savedcustomer.setCustID(cid);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            finally{
                this.lstCustomers.add(savedcustomer);
                this.tblCustomers.getSelectionModel().select(savedcustomer);
            }
        }
        else if (this.editMode.equals("update")){
            savedcustomer.setCustID(this.activeCustomer.getCustID());
            //      UPDATE customers
            //      update lstCustomers' customer object
            this.dbCtrl.updateCustomer(this.activeCustomer, savedcustomer);
            this.lstCustomers.set(this.activeCustomerIndex, savedcustomer);
        }
    }
    
    @FXML
    private void handleSelectCustomer(ActionEvent event){
        this.customerDisplay.setText(this.selectedCustomer.getCustName());
        // get a handle to the stage
        Stage stage = (Stage) this.btnSelect.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get TOP 300 customers from this.dbCtrl
        // Populate lstCustomers
        // Set tblCustomers to lstCustomers
        ArrayList<Customer> customers = this.dbCtrl.getCustomers();
        this.lstCustomers = FXCollections.observableArrayList(customers);
        this.tblCustomers.setItems(this.lstCustomers);
        
        
        // Set lsvwCustomers->OnSelect:
        //      this.editMode = 'update'
        //      Set txtName, txtAddress, and txtPhone .text = customer info
        if(this.editMode.equals("select")){
            this.btnNew.setDisable(true);
            this.btnDelete.setDisable(true);
            this.txtName.setDisable(true);
            this.txtAddress.setDisable(true);
            this.txtAddress2.setDisable(true);
            this.txtCity.setDisable(true);
            this.txtCountry.setDisable(true);
            this.txtPostalCode.setDisable(true);
            this.txtPhone.setDisable(true);
            this.chkActive.setDisable(true);
                   
            this.tblCustomers.setOnMousePressed(e ->{
                if (e.getClickCount() == 1 && e.isPrimaryButtonDown() ){
                   Customer customer = this.tblCustomers.getSelectionModel().getSelectedItem();
                   this.selectedCustomer.copyCustomer(customer);
                }
            });
            
            this.btnSave.setVisible(false);
            this.btnSelect.setVisible(true);
        }
        else{
            this.tblCustomers.setOnMousePressed(e ->{
                if (e.getClickCount() == 1 && e.isPrimaryButtonDown() ){
                   Customer customer = this.tblCustomers.getSelectionModel().getSelectedItem();
                   int index = this.tblCustomers.getSelectionModel().getSelectedIndex();
                   this.activeCustomer = customer;
                   this.activeCustomerIndex = index;

                   this.editMode = "update";

                   this.txtName.setText(this.activeCustomer.getCustName());
                   this.txtAddress.setText(this.activeCustomer.getCustAddress());
                   this.txtAddress2.setText(this.activeCustomer.getCustAddress2());
                   this.txtCity.setText(this.activeCustomer.getCustCity());
                   this.txtCountry.setText(this.activeCustomer.getCustCountry());
                   this.txtPostalCode.setText(this.activeCustomer.getCustPostalCode());
                   this.txtPhone.setText(this.activeCustomer.getCustPhone());
                }
            });
            
            this.btnSave.setVisible(true);
            this.btnSelect.setVisible(false);

            this.editMode = "new";
            this.chkActive.setSelected(true);
        }
        
    }    
    
}
