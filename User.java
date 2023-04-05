package com.server;

public class User {
    
    // This is not used any more after switched to using database

    private String username;
    private String password;
    private String email;

    public User(){
    }

    public User(String name, String pass, String mail){
        this.username = name;
        this.password = pass;
        this.email = mail;
    }

    public String getUserName(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String getEmail(){
        return this.email;
    }
}
