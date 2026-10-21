package com.group6.tasktracker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskHttpTest {
    @LocalServerPort int port;
    @Test void cookieLessClientCanFollowCreateRedirect() throws Exception {
        var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        var base = "http://127.0.0.1:" + port;
        var request = HttpRequest.newBuilder(URI.create(base+"/tasks"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("title=HTTP+redirect+check&category=College")).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("HTTP redirect check"));
        assertFalse(response.uri().getPath().contains(";jsessionid"));
    }
}
