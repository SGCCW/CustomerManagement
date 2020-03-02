/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customermanagement.Models;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Collin
 */
public class Customer {
    private int custID;
    private final SimpleStringProperty CustName = new SimpleStringProperty("");;
    private String custAddress;
    private String custAddress2;
    private String custCity;
    private String custCountry;
    private String custPostalCode;
    private String custPhone;
    private boolean isActive;
    
    public Customer(){
        this.custID = 0;
        this.custAddress = "";
        this.custAddress2 = "";
        this.custCity = "";
        this.custCountry = "";
        this.custPostalCode = "";
        this.custPhone = "";
        this.isActive = false;
    }
    public Customer(String name, 
                    String address, 
                    String address2,
                    String city,
                    String country,
                    String postalcode,
                    String phone,
                    boolean isactive){
        this.custID = 0;
        this.CustName.set(name);
        this.custAddress = address;
        this.custAddress2 = address2;
        this.custCity = city;
        this.custCountry = country;
        this.custPostalCode = postalcode;
        this.custPhone = phone;
        this.isActive = isactive;
    }
    public Customer(int id,
                    String name, 
                    String address, 
                    String address2,
                    String city,
                    String country,
                    String postalcode,
                    String phone,
                    boolean isactive){
        this.custID = id;
        this.CustName.set(name);
        this.custAddress = address;
        this.custAddress2 = address2;
        this.custCity = city;
        this.custCountry = country;
        this.custPostalCode = postalcode;
        this.custPhone = phone;
        this.isActive = isactive;
    }

    public int getCustID() {
        return custID;
    }
    
    public String getCustName() {
        return CustName.get();
    }

    public String getCustAddress() {
        return custAddress;
    }

    public String getCustAddress2() {
        return custAddress2;
    }

    public String getCustCity() {
        return custCity;
    }

    public String getCustCountry() {
        return custCountry;
    }

    public String getCustPostalCode() {
        return custPostalCode;
    }

    public String getCustPhone() {
        return custPhone;
    }
    public boolean getIsActive() {
        return isActive;
    }
    
    public void setCustID(int custID) {
        this.custID = custID;
    }
    public void setName(String custName){
        this.CustName.set(custName);
    }

    public void setCustAddress(String custAddress) {
        this.custAddress = custAddress;
    }

    public void setCustAddress2(String custAddress2) {
        this.custAddress2 = custAddress2;
    }

    public void setCustCity(String custCity) {
        this.custCity = custCity;
    }

    public void setCustCountry(String custCountry) {
        this.custCountry = custCountry;
    }

    public void setCustPostalCode(String custPostalCode) {
        this.custPostalCode = custPostalCode;
    }

    public void setCustPhone(String custPhone) {
        this.custPhone = custPhone;
    }
    
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public void copyCustomer(Customer customer){
        this.custID = customer.getCustID();
        this.CustName.set(customer.getCustName());
        this.custAddress = customer.getCustAddress();
        this.custAddress2 = customer.getCustAddress2();
        this.custCity = customer.getCustCity();
        this.custCountry = customer.getCustCountry();
        this.custPostalCode = customer.getCustPostalCode();
        this.custPhone = customer.getCustPhone();
        this.isActive = customer.getIsActive();
    }
    
    
    
}
