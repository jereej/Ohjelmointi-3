package com.server;
import java.sql.SQLException;
import org.json.JSONObject;
import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator{

    private MessageDatabase db = null;
    
    public UserAuthenticator(MessageDatabase db){
        // Authenticate users in /warning context
        super("warning");
        this.db = db;
    }

    @Override
    public boolean checkCredentials(String username, String password){

        boolean checkUser;
        try {
            // If authentication is successful, return true, else return false
            checkUser = db.authenticateUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        // Return the result of user authentication
        return checkUser;
    }

    public boolean addUser(String userName, String password, String email) throws SQLException{
        // Insert user into database
        boolean putToDB = db.setUser(new JSONObject()
        .put("username", userName)
        .put("password", password)
        .put("email", email));

        // If could not insert user into database, return false, else return true
        if (!putToDB){
            return false;
        }
        return true;
    }
    }

