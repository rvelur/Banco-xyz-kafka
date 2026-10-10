package cl.duoc.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reporte_transacciones_diarias")
public class ReporteTransaccionDiaria {

    @Id
    private Long id;
    private String fecha;
    private Double monto;
    private String tipo;
    private String estadoValidacion; // "VALIDO" o "ANOMALIA"
    private String detalleAnomalia;

    public ReporteTransaccionDiaria() {}

    public ReporteTransaccionDiaria(Long id, String fecha, Double monto, String tipo, String estadoValidacion, String detalleAnomalia) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.estadoValidacion = estadoValidacion;
        this.detalleAnomalia = detalleAnomalia;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstadoValidacion() { return estadoValidacion; }
    public void setEstadoValidacion(String estadoValidacion) { this.estadoValidacion = estadoValidacion; }
    public String getDetalleAnomalia() { return detalleAnomalia; }
    public void setDetalleAnomalia(String detalleAnomalia) { this.detalleAnomalia = detalleAnomalia; }
}