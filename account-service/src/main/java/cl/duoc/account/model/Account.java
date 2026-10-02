package cl.duoc.account.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private String accountId;
    private String clientName;
    private Double balance;
    private Integer age;
    private String accountType;
    private String status;

    public Account() {}

    public Account(String accountId, String clientName, Double balance, Integer age, String accountType, String status) {
        this.accountId = accountId;
        this.clientName = clientName;
        this.balance = balance;
        this.age = age;
        this.accountType = accountType;
        this.status = status;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}