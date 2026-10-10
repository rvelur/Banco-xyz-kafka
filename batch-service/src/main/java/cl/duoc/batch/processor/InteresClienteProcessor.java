package cl.duoc.batch.processor;

import org.springframework.batch.item.ItemProcessor;

import cl.duoc.batch.entity.CalculoInteresMensual;
import cl.duoc.batch.model.InteresCliente;

public class InteresClienteProcessor implements ItemProcessor<InteresCliente, CalculoInteresMensual> {

    @Override
    public CalculoInteresMensual process(InteresCliente item) {
        Long cuentaId = (item.getCuentaId() != null && !item.getCuentaId().isBlank()) 
                ? Long.parseLong(item.getCuentaId().trim()) : 0L;
        String cliente = item.getNombre() != null ? item.getNombre().trim() : "CLIENTE_DESCONOCIDO";
        String tipo = item.getTipo() != null ? item.getTipo().trim().toLowerCase() : "desconocido";

        double saldo = 0.0;
        if (item.getSaldo() != null && !item.getSaldo().isBlank()) {
            try {
                saldo = Double.parseDouble(item.getSaldo().trim());
            } catch (NumberFormatException ignored) {}
        }

        double tasa = 0.0;
        String estado = "APLICADO";

        switch (tipo) {
            case "ahorro":
                tasa = 0.015;
                break;
            case "prestamo":
                tasa = 0.025;
                break;
            case "hipoteca":
                tasa = 0.030;
                break;
            default:
                tasa = 0.0;
                estado = "IGNORADO_TIPO_INVALIDO";
                break;
        }

        double montoInteres = Math.round(saldo * tasa * 100.0) / 100.0;
        double nuevoSaldo = "ahorro".equals(tipo) ? (saldo + montoInteres) : (saldo - montoInteres);

        return new CalculoInteresMensual(cuentaId, cliente, saldo, tipo, tasa, montoInteres, nuevoSaldo, estado);
    }
}