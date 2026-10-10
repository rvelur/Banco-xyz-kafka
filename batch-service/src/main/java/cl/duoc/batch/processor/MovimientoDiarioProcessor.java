package cl.duoc.batch.processor;

import cl.duoc.batch.entity.ReporteTransaccionDiaria;
import cl.duoc.batch.model.MovimientoDiario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class MovimientoDiarioProcessor implements ItemProcessor<MovimientoDiario, ReporteTransaccionDiaria> {

    private static final Logger log = LoggerFactory.getLogger(MovimientoDiarioProcessor.class);

    @Override
    public ReporteTransaccionDiaria process(MovimientoDiario item) {
        Long id = Long.parseLong(item.getId().trim());
        String fecha = item.getFecha() != null ? item.getFecha().trim() : "SIN_FECHA";
        String tipo = item.getTipo() != null ? item.getTipo().trim().toLowerCase() : "desconocido";
        
        Double monto = 0.0;
        boolean esAnomalia = false;
        StringBuilder motivos = new StringBuilder();

        // 1. Validar presencia y formato numérico del monto
        if (item.getMonto() == null || item.getMonto().trim().isEmpty()) {
            esAnomalia = true;
            motivos.append("Monto vacio; ");
        } else {
            try {
                monto = Double.parseDouble(item.getMonto().trim());
                if (monto < 0) {
                    esAnomalia = true;
                    motivos.append("Monto negativo (").append(monto).append("); ");
                }
            } catch (NumberFormatException e) {
                esAnomalia = true;
                motivos.append("Monto no numerico; ");
            }
        }

        // 2. Validar clasificación de tipo de transacción
        if ("invalido".equals(tipo) || (!"credito".equals(tipo) && !"debito".equals(tipo))) {
            esAnomalia = true;
            motivos.append("Tipo de movimiento invalido (").append(tipo).append("); ");
        }

        String estado = esAnomalia ? "ANOMALIA" : "VALIDO";
        String detalle = esAnomalia ? motivos.toString() : "Operacion conforme a normativa bancaria";

        if (esAnomalia) {
            log.warn("Anomalia detectada en registro ID {}: {}", id, detalle);
        }

        return new ReporteTransaccionDiaria(id, fecha, monto, tipo, estado, detalle);
    }
}