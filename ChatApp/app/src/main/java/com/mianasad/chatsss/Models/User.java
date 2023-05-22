package com.mianasad.chatsss.Models;

public class User {

    private String uid, name, phoneNumber, profileImage, token, status, friends;

    public User() {

    }

    public User(String uid, String name, String friends) {
        this.uid = uid;
        this.name = name;
        this.friends = friends;
    }

    public User(String uid, String name, String phoneNumber, String profileImage, String token, String status) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.token = token;
        this.status = status;
    }

    public User(String uid, String name, String phoneNumber, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public String getAbout() {
        return status;
    }

    public void setAbout(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
