package com.server;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class WarningMessage {
    
    private String nickname;
    private Double latitude;
    private Double longitude;
    private String dangertype;
    private ZonedDateTime sent;
    private String areacode;
    private String phonenumber;
    
    // Constructor for message without areacode and phone number
    public WarningMessage(String nickname, Double latitude, Double longitude, ZonedDateTime sent, String dangertype){
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
        this.sent = sent;
        areacode = null;
        phonenumber = null;
    }

    // Constructor for message with areacode and phone number
    public WarningMessage(String nickname, Double latitude, Double longitude, ZonedDateTime sent, String dangertype, String areacode, String phonenumber){
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
        this.sent = sent;
        this.areacode = areacode;
        this.phonenumber = phonenumber;
    }

    public ZonedDateTime getSent() {
        return sent;
    }
    public void setSent(long epoch) {
        sent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    public long dateAsInt() {
        return sent.toInstant().toEpochMilli();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDangertype() {
        return dangertype;
    }

    public void setDangertype(String dangertype) {
        this.dangertype = dangertype;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

}
