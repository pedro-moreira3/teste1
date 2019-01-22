package br.com.lume.odonto.iugu;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Feature;

import org.glassfish.jersey.logging.LoggingFeature;

public class IuguConfiguration {

    private final static String URL = "https://api.iugu.com/v1";
    private final String tokenId;

    public IuguConfiguration(String token) {
        tokenId = token;
    }

    public Client getNewClient() {
        Client client = ClientBuilder.newClient().register(new Authenticator(tokenId, ""));
        Logger logger = Logger.getLogger(getClass().getName());
        Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        client.register(feature);
        return client;
    }

    public Client getNewClientNotAuth() {
        Client client = ClientBuilder.newClient();
        Logger logger = Logger.getLogger(getClass().getName());
        Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        client.register(feature);
        return client;
    }

    public static String url(String endpoint) {
        return URL + endpoint;
    }

}
