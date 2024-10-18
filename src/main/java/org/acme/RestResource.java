package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RestResource {
    @RestClient
    TestRestClient restClient;

    @GET
    @Path("greeting")
    public String greeting() {
        return restClient.greeting();
    }
}
