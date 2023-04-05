package com.server;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;



public class Server {
    
    private static MessageDatabase db = new MessageDatabase();

   private static SSLContext serverSSLContext(String file, String password) throws Exception{
    char[] passphrase = password.toCharArray();
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(new FileInputStream(file), passphrase);
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, passphrase);
    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(ks);
    SSLContext ssl = SSLContext.getInstance("TLS");
    ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    return ssl;

   }


    public static void main( String[] args ) throws Exception
    {
        try {
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
            SSLContext sslContext = serverSSLContext(args[0], args[1]);
            server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
            public void configure (HttpsParameters params) {
            params.getClientAddress();
            SSLContext c = getSSLContext();
            SSLParameters sslparams = c.getDefaultSSLParameters();
            params.setSSLParameters(sslparams);
            }
            });
         
        // Create warning context
         HttpContext warningContext = server.createContext("/warning", new WarningHandler(db));
         UserAuthenticator userAuth = new UserAuthenticator(db);    
         warningContext.setAuthenticator(userAuth);
         // Create registration context
         server.createContext("/registration", new RegistrationHandler(userAuth));
         server.setExecutor(Executors.newCachedThreadPool()); 
         server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
