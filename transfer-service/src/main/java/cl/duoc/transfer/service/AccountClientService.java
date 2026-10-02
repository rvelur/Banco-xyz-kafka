package cl.duoc.transfer.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class AccountClientService {

    private static final Logger log = LoggerFactory.getLogger(AccountClientService.class);

    @Value("${account.service.url:http://localhost:8081}")
    private String accountServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @CircuitBreaker(name = "accountServiceBreaker", fallbackMethod = "executeTransactionFallback")
    public void executeAccountMovement(String endpoint, String accountId, Double amount, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Double> payload = Map.of("amount", amount);
        HttpEntity<Map<String, Double>> entity = new HttpEntity<>(payload, headers);

        String url = accountServiceUrl + "/api/accounts/" + accountId + "/" + endpoint;
        restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
    }

    // Método Fallback invocado automáticamente cuando el circuito está abierto o hay fallo de red
    public void executeTransactionFallback(String endpoint, String accountId, Double amount, String authHeader, Throwable ex) {
        log.error("==> [CIRCUIT BREAKER ACTIVADO] Falló comunicación con account-service para cuenta: {}. Causa: {}", accountId, ex.getMessage());
        throw new IllegalStateException("Servicio de cuentas temporalmente no disponible (Circuito Abierto por Resiliencia). Reintente en unos instantes.");
    }
}