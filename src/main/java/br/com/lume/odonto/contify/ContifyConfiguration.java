package br.com.lume.odonto.contify;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Feature;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;

public class ContifyConfiguration {

    private final static String URL = "https://contify.com.br/api/v1";

    //public final static String TOKEN = "TEST_MjI1NGJiY2MxOTFiNTNmNTZjZTZiNzk5NDQ3YjFlYzcyOWQ4MzNmZTdhMWVlZDI5YzNiMGY0MWU2MzUxZjgxN2I0NjRhMzNiOTA5NGJmMzEzYTQ4Y2MxZmRmNjBjN2Y4OWE4NmEyMzY4MDZmN2Q4ZDc1MTMwZDBmZjQ5ZWFiNWE=";

    public ContifyConfiguration() {
    }

    public Client getNewClientNotAuth() {
        Client client = ClientBuilder.newClient();
        Logger logger = Logger.getLogger(getClass().getName());
        //Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        Feature feature = new LoggingFeature(logger, Level.INFO, Verbosity.PAYLOAD_ANY, 10000000);
        client.register(feature);
        return client;
    }

    public static String url(String endpoint) {
        return URL + endpoint;
    }

}
