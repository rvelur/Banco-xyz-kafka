package cl.duoc.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransferEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferEventConsumer.class);

    @KafkaListener(topics = "banco.transferencias", groupId = "banco-transfer-group")
    public void consumeTransferEvent(String message) {
        log.info("==> [KAFKA CONSUMER - AUDITORÍA] Evento de transferencia recibido con éxito: {}", message);
    }
}