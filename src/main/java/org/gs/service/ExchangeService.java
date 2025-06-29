package org.gs.service;

import org.gs.dto.ExchangeResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ExchangeService {
    private static final String FOREX_API_URL = "http://localhost:3000/forex/rates";

    public double getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        Client client = ClientBuilder.newClient();
        try {
            Response response = client.target(FOREX_API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                ExchangeResponse exchangeResponse = response.readEntity(ExchangeResponse.class);

                // If base currency is GHS (as in your example)
                if (exchangeResponse.getBase().equals(fromCurrency)) {
                    return exchangeResponse.getRates().get(toCurrency);
                } else {
                    // Need more complex logic if base currency isn't GHS
                    throw new WebApplicationException("Unsupported base currency", Response.Status.BAD_REQUEST);
                }
            } else {
                throw new WebApplicationException("Failed to fetch exchange rates",
                        Response.Status.SERVICE_UNAVAILABLE);
            }
        } finally {
            client.close();
        }
    }
}
