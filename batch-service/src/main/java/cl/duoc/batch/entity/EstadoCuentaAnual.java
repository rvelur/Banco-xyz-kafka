package cl.duoc.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estados_cuenta_anuales")
public class EstadoCuentaAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cuentaId;
    private String fechaOperacion;
    private String tipoTransaccion;
    private Double montoOriginal;
    private Double impactoContable; // Positivo (deposito/abono) o Negativo (compra/retiro)
    private String concepto;
    private String estadoAuditoria;

    public EstadoCuentaAnual() {}

    public EstadoCuentaAnual(Long cuentaId, String fechaOperacion, String tipoTransaccion,
                             Double montoOriginal, Double impactoContable, String concepto,
                             String estadoAuditoria) {
        this.cuentaId = cuentaId;
        this.fechaOperacion = fechaOperacion;
        this.tipoTransaccion = tipoTransaccion;
        this.montoOriginal = montoOriginal;
        this.impactoContable = impactoContable;
        this.concepto = concepto;
        this.estadoAuditoria = estadoAuditoria;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public String getFechaOperacion() { return fechaOperacion; }
    public void setFechaOperacion(String fechaOperacion) { this.fechaOperacion = fechaOperacion; }
    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
    public Double getMontoOriginal() { return montoOriginal; }
    public void setMontoOriginal(Double montoOriginal) { this.montoOriginal = montoOriginal; }
    public Double getImpactoContable() { return impactoContable; }
    public void setImpactoContable(Double impactoContable) { this.impactoContable = impactoContable; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public String getEstadoAuditoria() { return estadoAuditoria; }
    public void setEstadoAuditoria(String estadoAuditoria) { this.estadoAuditoria = estadoAuditoria; }
}