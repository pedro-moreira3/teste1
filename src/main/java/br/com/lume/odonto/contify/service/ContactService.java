package br.com.lume.odonto.contify.service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.com.lume.odonto.contify.ContifyConfiguration;
import br.com.lume.odonto.contify.model.Contact;
import br.com.lume.odonto.contify.response.ContifyResponse;

public class ContactService {

    private ContifyConfiguration contify;
    private final String CREATE_URL = ContifyConfiguration.url("/contact/insert");

    public ContactService(ContifyConfiguration contify) {
        this.contify = contify;
    }

    public ContifyResponse create(Contact contact) throws Exception {
        Response response = contify.getNewClientNotAuth().target(CREATE_URL).request().post(Entity.entity(contact, MediaType.APPLICATION_JSON));

        int ResponseStatus = response.getStatus();

        if (ResponseStatus == 200) {
            ContifyResponse r = response.readEntity(ContifyResponse.class);
            
            response.close();
            
            return r;
        }

        response.close();

        return null;
    }
}
