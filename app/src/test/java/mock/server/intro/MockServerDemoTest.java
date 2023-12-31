package mock.server.intro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

@ExtendWith(MockServerExtension.class)
class MockServerDemoTest {
    private final ClientAndServer clientAndServer;

    public MockServerDemoTest(ClientAndServer clientAndServer) {
        this.clientAndServer = clientAndServer;
    }

    @BeforeEach
    void beforeEach() {
        clientAndServer.reset();
    }

    @Test
    void testMethodAndPath() throws URISyntaxException, IOException, InterruptedException {
        clientAndServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/users"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("[{\"firstName\": \"foo\", \"lastName\": \"bar\"}]"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(new URI("http://localhost:" + clientAndServer.getPort() + "/users"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("[{\"firstName\": \"foo\", \"lastName\": \"bar\"}]", response.body());
        assertEquals(200, response.statusCode());
    }

    @Test
    void testPathParameters() throws URISyntaxException, IOException, InterruptedException {
        clientAndServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/users/{id}")
                        .withPathParameter("id", "1"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("{\"firstName\": \"foo\", \"lastName\": \"bar\"}"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(new URI("http://localhost:" + clientAndServer.getPort() + "/users/1"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("{\"firstName\": \"foo\", \"lastName\": \"bar\"}", response.body());
        assertEquals(200, response.statusCode());
    }

    @Test
    void testQueryParameters() throws URISyntaxException, IOException, InterruptedException {
        clientAndServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/users")
                        .withQueryStringParameter("firstName", "foo")
                        .withQueryStringParameter("lastName", "bar"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("{\"firstName\": \"foo\", \"lastName\": \"bar\"}"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(new URI("http://localhost:" + clientAndServer.getPort() +
                        "/users?firstName=foo&lastName=bar"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("{\"firstName\": \"foo\", \"lastName\": \"bar\"}", response.body());
        assertEquals(200, response.statusCode());
    }

    @Test
    void testHeaders() throws URISyntaxException, IOException, InterruptedException {
        clientAndServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/users")
                        .withHeader("Authorization", "CREDENTIALS"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("[{\"firstName\": \"foo\", \"lastName\": \"bar\"}]"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(new URI("http://localhost:" + clientAndServer.getPort() + "/users"))
                .header("Authorization", "CREDENTIALS")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("[{\"firstName\": \"foo\", \"lastName\": \"bar\"}]", response.body());
        assertEquals(200, response.statusCode());
    }

    @Test
    void testBody() throws URISyntaxException, IOException, InterruptedException {
        clientAndServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/users")
                        .withBody("{\"firstName\": \"foo\", \"lastName\": \"bar\"}"))
                .respond(response()
                        .withStatusCode(201)
                        .withBody("{\"id\": 1, \"firstName\": \"foo\", \"lastName\": \"bar\"}"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(new URI("http://localhost:" + clientAndServer.getPort() + "/users"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"firstName\": \"foo\", \"lastName\": \"bar\"}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("{\"id\": 1, \"firstName\": \"foo\", \"lastName\": \"bar\"}", response.body());
        assertEquals(201, response.statusCode());
    }

    @Test
    void testBodyJsonTemplate() throws URISyntaxException, IOException, InterruptedException {
        clientAndServer
                .when(request()
                        .withMethod("PUT")
                        .withPath("/users/{id}")
                        .withPathParameter("id", "1")
                        .withBody(json("{\"firstName\": \"${json-unit.any-string}\", \"lastName\": \"bar\"}")))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("{\"id\": 1, \"firstName\": \"someString\", \"lastName\": \"bar\"}"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(new URI("http://localhost:" + clientAndServer.getPort() + "/users/1"))
                .PUT(HttpRequest.BodyPublishers.ofString("{\"firstName\": \"someString\", \"lastName\": \"bar\"}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("{\"id\": 1, \"firstName\": \"someString\", \"lastName\": \"bar\"}", response.body());
        assertEquals(200, response.statusCode());
    }
}
