package cl.duoc.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "calculo_intereses_mensuales")
public class CalculoInteresMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cuentaId;
    private String cliente;
    private Double saldoBase;
    private String tipoProducto;
    private Double tasaInteres;
    private Double montoInteresCalculado;
    private Double nuevoSaldoEstimado;
    private String estadoProcesamiento;

    public CalculoInteresMensual() {}

    public CalculoInteresMensual(Long cuentaId, String cliente, Double saldoBase, String tipoProducto,
                                 Double tasaInteres, Double montoInteresCalculado, Double nuevoSaldoEstimado,
                                 String estadoProcesamiento) {
        this.cuentaId = cuentaId;
        this.cliente = cliente;
        this.saldoBase = saldoBase;
        this.tipoProducto = tipoProducto;
        this.tasaInteres = tasaInteres;
        this.montoInteresCalculado = montoInteresCalculado;
        this.nuevoSaldoEstimado = nuevoSaldoEstimado;
        this.estadoProcesamiento = estadoProcesamiento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public Double getSaldoBase() { return saldoBase; }
    public void setSaldoBase(Double saldoBase) { this.saldoBase = saldoBase; }
    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }
    public Double getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(Double tasaInteres) { this.tasaInteres = tasaInteres; }
    public Double getMontoInteresCalculado() { return montoInteresCalculado; }
    public void setMontoInteresCalculado(Double montoInteresCalculado) { this.montoInteresCalculado = montoInteresCalculado; }
    public Double getNuevoSaldoEstimado() { return nuevoSaldoEstimado; }
    public void setNuevoSaldoEstimado(Double nuevoSaldoEstimado) { this.nuevoSaldoEstimado = nuevoSaldoEstimado; }
    public String getEstadoProcesamiento() { return estadoProcesamiento; }
    public void setEstadoProcesamiento(String estadoProcesamiento) { this.estadoProcesamiento = estadoProcesamiento; }
}