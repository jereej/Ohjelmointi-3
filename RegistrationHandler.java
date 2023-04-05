package com.server;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler{

    private UserAuthenticator auth = null;

    public RegistrationHandler(UserAuthenticator userAuth){
        this.auth = userAuth;

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException{
        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")){
                handlePost(exchange);
            } else {
                sendMessage("Not supported", 400, exchange);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
        }
        
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException{
        Headers headers = exchange.getRequestHeaders();
        String contentType = "";
        JSONObject obj = null;

        // Check for content-type
        if (!headers.containsKey("Content-Type")){
            sendMessage("No content type in request", 411, exchange);
        }

        contentType = headers.get("Content-Type").get(0);

        // Check the content type
        if (!contentType.equalsIgnoreCase("application/json")){
            sendMessage("Content type is not application/json", 407, exchange);
        }

        InputStream inputStream = exchange.getRequestBody();
        String userRegistration = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        .lines().collect(Collectors.joining("\n"));
        inputStream.close();

        // If the registration message is not empty or null, create JSONObject
        if (userRegistration.length() != 0 || userRegistration != null){
            try {
                obj = new JSONObject(userRegistration);
            } catch (JSONException e) {
                System.out.println("json parse error, faulty user json");
            }
        
        // Check the username and password, if they are not empty, try to add user into database
        if(obj.getString("username").length() != 0  || obj.getString("password").length() != 0){
            Boolean result = auth.addUser(obj.getString("username"), obj.getString("password"), obj.getString("email"));
            if (result){
            sendMessage("User registered successfully", 200, exchange);
            } 
            sendMessage("User already exists", 405, exchange);
        } 
        sendMessage("No proper user credentials", 413, exchange);
        }
        sendMessage("No user credentials", 412, exchange);
     }

    // Method for sending a message with message and responsecode as parameters
    private void sendMessage(String message, int rCode, HttpExchange exchange) throws IOException{
        byte [] bytes = message.getBytes("UTF-8");
        exchange.sendResponseHeaders(rCode, bytes.length);
        OutputStream output = exchange.getResponseBody();
        output.write(bytes);
        output.flush();
        output.close();
    }
}
