package br.com.lume.odonto.iugu.services;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.com.lume.odonto.iugu.IuguConfiguration;
import br.com.lume.odonto.iugu.exceptions.IuguException;
import br.com.lume.odonto.iugu.model.Charge;
import br.com.lume.odonto.iugu.responses.ChargeResponse;

public class ChargeService {

    private IuguConfiguration iugu;
    private final String CREATE_URL = IuguConfiguration.url("/charge");

    public ChargeService(IuguConfiguration iuguConfiguration) {
        this.iugu = iuguConfiguration;
    }

    public ChargeResponse create(Charge charge) throws IuguException {
        Response response = this.iugu.getNewClient().target(CREATE_URL).request().post(Entity.entity(charge, MediaType.APPLICATION_JSON));

        int ResponseStatus = response.getStatus();
        String ResponseText = null;

        if (ResponseStatus == 200)
            return response.readEntity(ChargeResponse.class);

        // Error Happened
        if (response.hasEntity())
            ResponseText = response.readEntity(String.class);

        response.close();

        throw new IuguException("Error creating charge!", ResponseStatus, ResponseText);
    }
}
