package com.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;


public class WarningHandler implements HttpHandler{
    
    private MessageDatabase db = null;
    
    public WarningHandler(MessageDatabase db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException{

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")){
            handlePost(exchange);
            
        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                handleGet(exchange);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("SQL exception in get function");
            }
        } else {
            sendMessage("Not supported", 400, exchange);
        }
    }

    
    private void handlePost(HttpExchange exchange) throws IOException {
        WarningMessage warning = null;
        Headers headers = exchange.getRequestHeaders();
        String contentType = "";
        JSONObject obj = null;
    
        try {
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
            String messageString = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));
            inputStream.close();

            // if post-message is empty, send message and headers
            if (messageString.length() == 0 || messageString == null){
                sendMessage("Message cannot be empty", 412, exchange);
            }

            // Create JSONObject
            try {
                obj = new JSONObject(messageString);
            } catch (JSONException e) {
                System.out.println("json parse error, faulty user json");
            }

            // Check the JSONObject for query
            if (obj.has("query")){
                // If the query is of correct type (user, time, location), send the query to MessageDatabase's queryMessage method
                if (obj.getString("query").equals("user") || obj.getString("query").equals("time")
                || obj.getString("query").equals("location")){
                    String query = db.queryMessage(obj);
                    sendMessage(query, 200, exchange);
                } else {
                    // If query type is wrong, send message and responsecode to user
                    sendMessage("QUERY TYPE WRONG", 412, exchange);
                }
            }
    
            // Check the JSONObject for valitidy, if it has the correct parameters with correct datatypes
            // Start creating the warning message
            else if((obj.get("longitude") instanceof Double || obj.get("longitude") instanceof BigDecimal) && (obj.get("latitude") instanceof Double ||
                obj.get("latitude") instanceof BigDecimal) && (obj.get("sent") instanceof String) && obj.getString("dangertype").equals("Moose")
                || obj.getString("dangertype").equals("Reindeer") || obj.getString("dangertype").equals("Deer") || obj.getString("dangertype").equals("Other")){
        
                LocalDateTime checkTime = OffsetDateTime.parse((CharSequence) obj.get("sent")).toLocalDateTime();
                ZonedDateTime zoneDateTime = checkTime.atZone(ZoneId.of("UTC"));

                // Check for areacode, if the message has an areacode, it also has a phone number
                if (obj.has("areacode")){
                    // Create the warning message with areacode and phone number
                    warning = new WarningMessage(obj.getString("nickname"), obj.getDouble("latitude"),
                    obj.getDouble("longitude"), zoneDateTime, obj.getString("dangertype"),
                    obj.getString("areacode"), obj.getString("phonenumber"));
                } else {
                    // If it does not have an areacode, create a warning message without areacode and phonenumber
                    warning = new WarningMessage(obj.getString("nickname"), obj.getDouble("latitude"),
                    obj.getDouble("longitude"), zoneDateTime, obj.getString("dangertype"));
                }
                // Insert the message to database
                db.setMessage(warning);
                sendMessage("Post successful", 200, exchange);

            } else {
                sendMessage("Incorrect message", 400, exchange);
            }
    
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            sendMessage("Internal server error", 500, exchange);
        }
       }

   private void handleGet(HttpExchange exchange) throws IOException, SQLException {
    // Handle GET request
    String response = db.getMessages();
    sendMessage(response, 200, exchange);

   }

   // Send message with string and responsecode as parameters
   private void sendMessage(String message, int rCode, HttpExchange exchange) throws IOException{
    byte [] bytes = message.getBytes("UTF-8");
    exchange.sendResponseHeaders(rCode, bytes.length);
    OutputStream output = exchange.getResponseBody();
    output.write(bytes);
    output.flush();
    output.close();
}
}
