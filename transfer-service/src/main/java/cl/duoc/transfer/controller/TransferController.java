package cl.duoc.transfer.controller;

import cl.duoc.transfer.dto.TransferRequest;
import cl.duoc.transfer.service.AccountClientService;
import cl.duoc.transfer.service.TransferEventProducer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final AccountClientService accountClientService;
    private final TransferEventProducer transferEventProducer;

    public TransferController(AccountClientService accountClientService, TransferEventProducer transferEventProducer) {
        this.accountClientService = accountClientService;
        this.transferEventProducer = transferEventProducer;
    }

    @PostMapping
    public ResponseEntity<?> processTransfer(
            @RequestBody TransferRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        if (request.getAmount() == null || request.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "El monto de transferencia debe ser superior a 0"));
        }

        try {
            // 1. Debitar en cuenta origen (protegido por Circuit Breaker)
            accountClientService.executeAccountMovement("withdraw", request.getSourceAccountId(), request.getAmount(), authHeader);

            // 2. Acreditar en cuenta destino (protegido por Circuit Breaker)
            accountClientService.executeAccountMovement("deposit", request.getTargetAccountId(), request.getAmount(), authHeader);

            String transactionId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // 3. Emitir evento asíncrono a Apache Kafka
            transferEventProducer.emitTransferEvent(
                    transactionId,
                    request.getSourceAccountId(),
                    request.getTargetAccountId(),
                    request.getAmount()
            );

            // 4. Respuesta transaccional exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", transactionId);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("status", "SUCCESS");
            response.put("sourceAccountId", request.getSourceAccountId());
            response.put("targetAccountId", request.getTargetAccountId());
            response.put("amount", request.getAmount());
            response.put("description", request.getDescription());
            response.put("message", "Transferencia bancaria procesada, protegida con Circuit Breaker y emitida a Kafka.");

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "error", "Operación bancaria denegada",
                "details", ex.getResponseBodyAsString()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "CIRCUIT_BREAKER_FALLBACK",
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Falla inesperada en procesamiento de transferencia",
                "details", ex.getMessage()
            ));
        }
    }
}