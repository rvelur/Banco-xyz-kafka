package cl.duoc.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransferEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TransferEventProducer.class);
    private static final String TOPIC = "banco.transferencias";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransferEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void emitTransferEvent(String transactionId, String sourceId, String targetId, Double amount) {
        String message = String.format("{\"transactionId\":\"%s\",\"source\":\"%s\",\"target\":\"%s\",\"amount\":%.2f,\"status\":\"COMPLETED\"}",
                transactionId, sourceId, targetId, amount);

        log.info("==> [KAFKA PRODUCER] Publicando evento de transferencia en tópico '{}': {}", TOPIC, message);
        kafkaTemplate.send(TOPIC, transactionId, message);
    }
}