package uk.gov.hmcts.reform.civil.cftlib;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * IDAM's {@code /loginUser}, which civil's Playwright tests sign in with, over the IDAM simulator's password grant.
 * The tests use this service as their IDAM API under cftlib; {@code /o/userinfo} passes through to the simulator.
 */
@RestController
public class LocalIdamController {

    static final String LOGIN_USER = "/loginUser";
    static final String USER_INFO = "/o/userinfo";
    private static final String IDAM_SIMULATOR = "http://localhost:5062";

    private final HttpClient client = HttpClient.newHttpClient();

    @PostMapping(value = LOGIN_USER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loginUser(@RequestParam String username, @RequestParam String password)
        throws IOException, InterruptedException {
        String form = "grant_type=password&client_id=civil-service&client_secret=cftlib&scope="
            + encode("openid profile roles") + "&username=" + encode(username) + "&password=" + encode(password);
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder(URI.create(IDAM_SIMULATOR + "/o/token"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        return ResponseEntity.status(response.statusCode()).contentType(MediaType.APPLICATION_JSON).body(response.body());
    }

    @GetMapping(value = USER_INFO, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> userInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization)
        throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder(URI.create(IDAM_SIMULATOR + USER_INFO))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        return ResponseEntity.status(response.statusCode()).contentType(MediaType.APPLICATION_JSON).body(response.body());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
