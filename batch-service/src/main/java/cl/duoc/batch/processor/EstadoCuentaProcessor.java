package cl.duoc.batch.processor;

import org.springframework.batch.item.ItemProcessor;

import cl.duoc.batch.entity.EstadoCuentaAnual;
import cl.duoc.batch.model.MovimientoAnual;

public class EstadoCuentaProcessor implements ItemProcessor<MovimientoAnual, EstadoCuentaAnual> {

    @Override
    public EstadoCuentaAnual process(MovimientoAnual item) {
        Long cuentaId = (item.getCuentaId() != null && !item.getCuentaId().isBlank())
                ? Long.parseLong(item.getCuentaId().trim()) : 0L;
        String fecha = (item.getFecha() != null && !item.getFecha().isBlank()) ? item.getFecha().trim() : "SIN_FECHA";
        String transaccion = (item.getTransaccion() != null) ? item.getTransaccion().trim().toLowerCase() : "desconocido";
        String descripcion = (item.getDescripcion() != null && !item.getDescripcion().isBlank())
                ? item.getDescripcion().trim() : "Sin descripcion";

        double monto = 0.0;
        if (item.getMonto() != null && !item.getMonto().isBlank()) {
            try {
                monto = Math.abs(Double.parseDouble(item.getMonto().trim()));
            } catch (NumberFormatException ignored) {}
        }

        double impactoContable;
        String estadoAuditoria = "CONFORME";

        // Determinar impacto financiero según tipo de movimiento bancario
        if ("deposito".equals(transaccion) || "pago".equals(transaccion) || "abono".equals(transaccion)) {
            impactoContable = monto;
        } else if ("compra".equals(transaccion) || "retiro".equals(transaccion)) {
            impactoContable = -monto;
        } else {
            impactoContable = 0.0;
            estadoAuditoria = "REVISION_TIPO_DESCONOCIDO";
        }

        if (monto == 0.0) {
            estadoAuditoria = "ALERTA_MONTO_CERO";
        }

        return new EstadoCuentaAnual(cuentaId, fecha, transaccion, monto, impactoContable, descripcion, estadoAuditoria);
    }
}