package com.bixin.bluetooth.model.bean;

public class PhoneBook {
    private String name;
    private String number;

    public PhoneBook() {

    }

    public PhoneBook(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
