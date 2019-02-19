package br.com.lume.odonto.iugu.services;

import br.com.lume.odonto.iugu.IuguConfiguration;
import br.com.lume.odonto.iugu.exceptions.IuguException;
import br.com.lume.odonto.iugu.model.PaymentToken;
import br.com.lume.odonto.iugu.responses.PaymentTokenResponse;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PaymentTokenService {

	private IuguConfiguration iugu;
	private final String CREATE_URL = IuguConfiguration.url("/payment_token");

	public PaymentTokenService(IuguConfiguration iuguConfiguration) {
		this.iugu = iuguConfiguration;
	}

	public PaymentTokenResponse create(PaymentToken paymentToken) throws IuguException {
		Response response = this.iugu.getNewClientNotAuth().target(CREATE_URL).request().post(Entity.entity(paymentToken, MediaType.APPLICATION_JSON));

		int ResponseStatus = response.getStatus();
		String ResponseText = null;

		if (ResponseStatus == 200)
			return response.readEntity(PaymentTokenResponse.class);

		// Error Happened
		if (response.hasEntity())
			ResponseText = response.readEntity(String.class);

		response.close();

		throw new IuguException("Error creating token!", ResponseStatus, ResponseText);
	}
}
