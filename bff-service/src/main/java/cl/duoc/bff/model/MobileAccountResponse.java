package cl.duoc.bff.model;

public class MobileAccountResponse {
    private String id;
    private Double saldo;
    private String moneda;

    public MobileAccountResponse() {}

    public MobileAccountResponse(String id, Double saldo, String moneda) {
        this.id = id;
        this.saldo = saldo;
        this.moneda = moneda;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
}