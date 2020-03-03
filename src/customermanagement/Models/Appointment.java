/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customermanagement.Models;

import java.time.LocalDateTime;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Collin
 */
public class Appointment {
    private int appointmentId;
    private Customer customer;
    private SimpleStringProperty customerName = new SimpleStringProperty("");;
    private int userId;
    private String title;
    private String description;
    private SimpleStringProperty location = new SimpleStringProperty("");;
    private SimpleStringProperty type = new SimpleStringProperty("");;
    private LocalDateTime start;
    private LocalDateTime end;
    private SimpleStringProperty startstr = new SimpleStringProperty("");;

    
    public Appointment(String customername, String location, String type, String start){
        this.customerName.set(customername);
        this.location.set(location);
        this.type.set(type);
        this.startstr.set(start);
    }
    
    public Appointment(int appointmentid,
                            Customer customer,
                            int userid,
                            String title,
                            String description,
                            String location,
                            String type,
                            LocalDateTime start,
                            LocalDateTime end){
        this.appointmentId = appointmentid;
        this.customer = customer;
        this.customerName.set(customer.getCustName());
        this.userId = userid;
        this.title = title;
        this.description = description;
        this.location.set(location);
        this.type.set(type);
        this.start = start;
        this.startstr.set(start.toString());
        this.end = end;
    }
    
    public Appointment(Customer customer,
                            int userid,
                            String title,
                            String description,
                            String location,
                            String type,
                            LocalDateTime start,
                            LocalDateTime end){
        this.appointmentId = -1;
        this.customer = customer;
        this.customerName.set(customer.getCustName());
        this.userId = userid;
        this.title = title;
        this.description = description;
        this.location.set(location);
        this.type.set(type);
        this.start = start;
        this.startstr.set(start.toString());
        this.end = end;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getCustomerName() {
        return customerName.get();
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location.get();
    }

    public String getType() {
        return type.get();
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setCustomerName(String customerName) {
        this.customerName.set(customerName);
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
        this.startstr.set(start.toString());
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
    
    
}
