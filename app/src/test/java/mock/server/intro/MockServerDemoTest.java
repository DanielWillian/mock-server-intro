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
}
