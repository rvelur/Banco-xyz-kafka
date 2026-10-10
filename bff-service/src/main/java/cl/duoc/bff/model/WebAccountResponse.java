package cl.duoc.bff.model;

import java.util.Map;

public class WebAccountResponse {
    private String id;
    private String cliente;
    private Double saldo;
    private String tipoCuenta;
    private String estado;
    private Map<String, Object> detallesAuditoria;

    public WebAccountResponse() {}

    public WebAccountResponse(String id, String cliente, Double saldo, String tipoCuenta, String estado, Map<String, Object> detallesAuditoria) {
        this.id = id;
        this.cliente = cliente;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.estado = estado;
        this.detallesAuditoria = detallesAuditoria;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Map<String, Object> getDetallesAuditoria() { return detallesAuditoria; }
    public void setDetallesAuditoria(Map<String, Object> detallesAuditoria) { this.detallesAuditoria = detallesAuditoria; }
}