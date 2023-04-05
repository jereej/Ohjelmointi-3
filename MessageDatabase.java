package com.server;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;


public class MessageDatabase {

    private Connection dbConnection = null;
    private String database = "MessageDB";
    

    MessageDatabase() {
        try {
            open(database);
        } catch (SQLException e) {
            System.out.println("Error in messageDatabase constructor");
        }
    }

    public void open(String dbName) throws SQLException{
        // Opens the database if it exists
        String database = "jdbc:sqlite:" + dbName;
        File path = new File(dbName);
        boolean databaseExists = path.exists();
        dbConnection = DriverManager.getConnection(database);

        if (!databaseExists){
            // Initializes the database if one does not exist
            initializeDatabase(dbName);
        }
    }

    public boolean initializeDatabase(String dbName) throws SQLException{
        if (null != dbConnection) {
            // Creates table for users
            String createUserTable = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50), primary key(username))";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.close();

            // Creates table for messages
            String createMessageTable = "CREATE TABLE messages (nickname varchar(50) NOT NULL, latitude REAL(50) NOT NULL, longitude REAL(50) NOT NULL, sent varchar(50) NOT NULL, dangertype varchar(50) NOT NULL, areacode varchar(50), phonenumber varchar(50))";
            Statement createMessageStatement = dbConnection.createStatement();
            createMessageStatement.executeUpdate(createMessageTable);
            createMessageStatement.close();
            return true;
        }
        return false;
    }

    public boolean setUser(JSONObject user) throws SQLException {
        
        // If user does not exist, create user in the database
        if (checkIfUserExists(user.getString("username"))){
            return false;
        }
        String setUserStr = "insert into users " + "VALUES('" + user.getString("username") +
        "','" + Crypt.crypt(user.getString("password")) + "','" + user.getString("email") + "')";
        Statement createStatement;
        createStatement = dbConnection.createStatement();
		createStatement.executeUpdate(setUserStr);
		createStatement.close();
        return true;
    }

    public boolean checkIfUserExists(String username) throws SQLException{
        Statement queryStatement = null;
        ResultSet resSet;

        // Create SQL query for checking user
        String checkUser = "select username from users where username = '" + username + "'";
        queryStatement = dbConnection.createStatement();
		resSet = queryStatement.executeQuery(checkUser);

        // Return true if user exists, false if it does not
        if (resSet.next()){
            return true;
        }
        return false;
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        Statement queryStatement = null;
        ResultSet resSet;

        String getMessagesString = "select username, password from users where username = '" + username + "'";
        queryStatement = dbConnection.createStatement();
		resSet = queryStatement.executeQuery(getMessagesString);


        if (resSet.next()){
            // Compare hashed password from database to the password coming in as a parameter
            String hashedPw = resSet.getString("password");
            // If the password matches, return true, otherwise false
            return hashedPw.equals(Crypt.crypt(password, hashedPw));
        }

        // If authentication failed, return false
        return false;
    }

    public void setMessage(WarningMessage message) throws SQLException {
        // SQL query for inserting the message to the database
		String insertString = "INSERT INTO messages " + "VALUES('" + message.getNickname() + "','" + message.getLatitude()
        + "','" + message.getLongitude() + "','" + message.dateAsInt() + "','" + message.getDangertype()
         + "','" + message.getAreacode() + "','" + message.getPhonenumber() + "')";
        Statement createStatement;
		createStatement = dbConnection.createStatement();
		createStatement.executeUpdate(insertString);
		createStatement.close();
    }

    public String getMessages() throws SQLException {
        JSONArray arr = new JSONArray();

        Statement queryStatement = null;

        // SQL query for selecting information from the database
        String getMessagesString = "SELECT nickname, latitude, longitude, sent, dangertype, areacode, phonenumber FROM messages";

        queryStatement = dbConnection.createStatement();
		ResultSet rs = queryStatement.executeQuery(getMessagesString);

        while (rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("nickname", rs.getString(1))
            .put("latitude", rs.getDouble(2))
            .put("longitude", rs.getDouble(3));
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong(4)), ZoneOffset.UTC);
            obj.put("sent", zdt);
            obj.put("dangertype", rs.getString(5));
            if (!rs.getString("areacode").equals("null")){
            obj.put("areacode", rs.getString(6));
            obj.put("phonenumber", rs.getString(7));
            }
            arr.put(obj);
		}
        return arr.toString();
    }

    public void closeDB() throws SQLException {
		if (null != dbConnection) {
			dbConnection.close();
            System.out.println("closing db connection");
			dbConnection = null;
		}
    }

    public String queryMessage(JSONObject obj) throws SQLException{
        // Function that creates and executes the queries for key "query"
        JSONArray arr = new JSONArray();
        Statement queryStatement = null;
        ResultSet rs;

        // This is where the querying with the time parameter happens
        if (obj.getString("query").equals("time")){
           // Get the timestart and timeend from the JSONObject
            String startTime = obj.getString("timestart");
            String endTime = obj.getString("timeend");
            
            // Convert the timestart and timeend to long
            LocalDateTime checkStartTime = OffsetDateTime.parse((CharSequence)startTime).toLocalDateTime();
            ZonedDateTime startZoneDateTime = checkStartTime.atZone(ZoneId.of("UTC"));
            LocalDateTime checkEndTime = OffsetDateTime.parse((CharSequence)endTime).toLocalDateTime();
            ZonedDateTime endZoneDateTime = checkEndTime.atZone(ZoneId.of("UTC"));
            long startTimeAsLong = startZoneDateTime.toInstant().toEpochMilli();
            long endTimeAsLong = endZoneDateTime.toInstant().toEpochMilli();
           
            // Select all data between the times
            String timeQuery = "SELECT * FROM messages Where sent BETWEEN '" + startTimeAsLong + "' AND '" + endTimeAsLong + "'";
            queryStatement = dbConnection.createStatement();
		    rs = queryStatement.executeQuery(timeQuery);
           
            // After executing the query, fetch the messages from the database
            while (rs.next()) {
                JSONObject tempObj = new JSONObject();
                tempObj.put("nickname", rs.getString(1))
                .put("latitude", rs.getDouble(2))
                .put("longitude", rs.getDouble(3));
                ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong(4)), ZoneOffset.UTC);
                tempObj.put("sent", zdt);
                tempObj.put("dangertype", rs.getString(5));
                if (!rs.getString("areacode").equals("null")){
                tempObj.put("areacode", rs.getString(6));
                tempObj.put("phonenumber", rs.getString(7));
                }
                arr.put(tempObj);
            }
            // Return the messages
            return arr.toString(); 
        }
        
        if (obj.getString("query").equals("location")){
            // Get the specific longitudes and latitudes from JSONObject
            Double upperlongitude = obj.getDouble("uplongitude");
            Double lowerlongitude = obj.getDouble("downlongitude");
            Double upperlatitude = obj.getDouble("uplatitude");
            Double lowerlatitude = obj.getDouble("downlatitude");
           
            // SQL statement for querying the location
            String locationQuery = "SELECT * FROM messages WHERE (latitude BETWEEN " + lowerlatitude + " AND " + upperlatitude +
            ") AND (longitude BETWEEN " + upperlongitude + " AND " + lowerlongitude + ")";
            
            queryStatement = dbConnection.createStatement();
		    rs = queryStatement.executeQuery(locationQuery);
           
            // After executing the query, fetch the messages from the database
            while (rs.next()) {
                JSONObject tempObj = new JSONObject();
                tempObj.put("nickname", rs.getString(1))
                .put("latitude", rs.getDouble(2))
                .put("longitude", rs.getDouble(3));
                ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong(4)), ZoneOffset.UTC);
                tempObj.put("sent", zdt);
                tempObj.put("dangertype", rs.getString(5));
                if (!rs.getString("areacode").equals("null")){
                tempObj.put("areacode", rs.getString(6));
                tempObj.put("phonenumber", rs.getString(7));
                }
                arr.put(tempObj);
            }
            // Return the messages
            return arr.toString(); 
        }

        // If the query is not of type time or location, it is of type user
        // Select all data under a specific nickname
        String nickQuery = "SELECT * FROM messages WHERE nickname LIKE '%" + obj.getString("nickname") + "%'";
        queryStatement = dbConnection.createStatement();
		rs = queryStatement.executeQuery(nickQuery);

        // After executing the query, fetch the messages from the database
        while (rs.next()) {
            JSONObject tempObj = new JSONObject();
            tempObj.put("nickname", rs.getString(1))
            .put("latitude", rs.getDouble(2))
            .put("longitude", rs.getDouble(3));
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong(4)), ZoneOffset.UTC);
            tempObj.put("sent", zdt);
            tempObj.put("dangertype", rs.getString(5));
            if (!rs.getString("areacode").equals("null")){
            tempObj.put("areacode", rs.getString(6));
            tempObj.put("phonenumber", rs.getString(7));
            }
            arr.put(tempObj);
        }
        // Return the messages
        return arr.toString(); 
            

    }

}
