package com.bixin.bluetooth.model.db;

public class CallLogInfo {
    private String name;//通话人姓名
    private String number;//通话人号码
    private int type;//通话类型：6|接听、4|拨出、5|未接

    public CallLogInfo(String name, String number, int type) {
        this.name = name;
        this.number = number;
        this.type = type;
    }

    public CallLogInfo() {

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
