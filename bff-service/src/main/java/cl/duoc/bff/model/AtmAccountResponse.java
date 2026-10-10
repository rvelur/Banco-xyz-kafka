package cl.duoc.bff.model;

public class AtmAccountResponse {
    private String cuentaId;
    private Double saldoDisponible;
    private Double cupoMaximoGiro;
    private String estadoOperativo;

    public AtmAccountResponse() {}

    public AtmAccountResponse(String cuentaId, Double saldoDisponible, Double cupoMaximoGiro, String estadoOperativo) {
        this.cuentaId = cuentaId;
        this.saldoDisponible = saldoDisponible;
        this.cupoMaximoGiro = cupoMaximoGiro;
        this.estadoOperativo = estadoOperativo;
    }

    public String getCuentaId() { return cuentaId; }
    public void setCuentaId(String cuentaId) { this.cuentaId = cuentaId; }
    public Double getSaldoDisponible() { return saldoDisponible; }
    public void setSaldoDisponible(Double saldoDisponible) { this.saldoDisponible = saldoDisponible; }
    public Double getCupoMaximoGiro() { return cupoMaximoGiro; }
    public void setCupoMaximoGiro(Double cupoMaximoGiro) { this.cupoMaximoGiro = cupoMaximoGiro; }
    public String getEstadoOperativo() { return estadoOperativo; }
    public void setEstadoOperativo(String estadoOperativo) { this.estadoOperativo = estadoOperativo; }
}