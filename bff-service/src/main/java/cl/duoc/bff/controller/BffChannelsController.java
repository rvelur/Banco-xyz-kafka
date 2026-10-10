package cl.duoc.bff.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/bff")
public class BffChannelsController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String ACCOUNT_SERVICE_URL = "http://account-service:8081/api/accounts/";

    private HttpHeaders createHeaders(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }
        return headers;
    }

    @GetMapping("/web/cuentas/{id}")
    public ResponseEntity<?> getCuentaWeb(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders(authHeader));
            ResponseEntity<Map> response = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL + id,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @GetMapping("/mobile/cuentas/{id}")
    public ResponseEntity<?> getCuentaMobile(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders(authHeader));
            ResponseEntity<Map> response = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL + id,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @GetMapping("/atm/cuentas/{id}")
    public ResponseEntity<?> getCuentaAtm(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders(authHeader));
            ResponseEntity<Map> response = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL + id,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}