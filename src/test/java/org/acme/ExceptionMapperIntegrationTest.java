package org.acme;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import java.net.HttpURLConnection;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class ExceptionMapperIntegrationTest {

    static WireMockServer wireMockServer = new WireMockServer(ConfigProvider.getConfig().getValue("port", Integer.class));

    @Inject
    @RestClient
    TestRestClient testRestClient;

    @BeforeAll
    static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @ParameterizedTest(name = """
            Given an external REST service responding with status code {0},
            when calling the REST server,
            then the response code is 500 and the external REST service response is not returned""")
    @ValueSource(ints = {
            HttpURLConnection.HTTP_NOT_FOUND})
    void clientResponseIsNotReturnByServer(int clientStatus) {
        String clientResponse = "an error occured";
        mockExternalRest(clientStatus, clientResponse);

        String serverResponse = given()
                .when().get("/greeting")
                .then().statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .extract().body().asString();

        assertFalse(serverResponse.contains(clientResponse));
    }

    @Test
    @DisplayName("""
            Given an external REST service responding with status code 404,
            when calling the REST client,
            then an exception with a NotFoundException cause is thrown
            """)
    void exceptionsAreMapped() {
        Class<NotFoundException> expectedCause = NotFoundException.class;
        int expectedStatusCode = 404;
        mockExternalRest(expectedStatusCode, "");

        ClientWebApplicationException exception = assertThrows(ClientWebApplicationException.class,
                () -> testRestClient.greeting());

        assertEquals(expectedStatusCode, exception.getResponse().getStatus());
        assertTrue(expectedCause.isInstance(exception.getCause()));
        WebApplicationException cause = expectedCause.cast(exception.getCause());
        assertEquals(expectedStatusCode, cause.getResponse().getStatus());
    }

    @Test
    @DisplayName("""
            Given a response with a status code with no exception mapper,
            when calling the REST client,
            then an exception with a standard WebApplicationException cause is thrown""")
    void exceptionNotMapped() {
        int expectedStatusCode = HttpURLConnection.HTTP_VERSION;
        mockExternalRest(expectedStatusCode, "");

        ClientWebApplicationException exception = assertThrows(ClientWebApplicationException.class,
                () -> testRestClient.greeting());

        assertInstanceOf(WebApplicationException.class, exception.getCause());
        assertEquals(expectedStatusCode, exception.getResponse().getStatus());
    }

    private static void mockExternalRest(int clientStatus, String clientResponse) {
        wireMockServer.stubFor(get(urlEqualTo("/greeting")).willReturn(
                aResponse()
                        .withStatus(clientStatus)
                        .withBody(clientResponse)));
    }
}