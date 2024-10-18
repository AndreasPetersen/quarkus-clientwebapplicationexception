package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "test-rest-client")
public interface TestRestClient {
    @GET
    @Path("greeting")
    String greeting();
}
