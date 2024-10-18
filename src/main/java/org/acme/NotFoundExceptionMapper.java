package org.acme;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.net.HttpURLConnection;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class NotFoundExceptionMapper
        implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status == HttpURLConnection.HTTP_NOT_FOUND;
    }

    @Override
    public WebApplicationException toThrowable(Response response) {
        return new NotFoundException(response);
    }
}