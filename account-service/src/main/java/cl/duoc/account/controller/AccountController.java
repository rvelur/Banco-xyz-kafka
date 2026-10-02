package cl.duoc.account.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.account.model.Account;
import cl.duoc.account.repository.AccountRepository;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // 1. Listar todas las cuentas cargadas desde el CSV
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // 2. Consultar saldo y detalle de una cuenta específica
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable String accountId) {
        return accountRepository.findById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // 3. Realizar un Retiro de dinero
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable String accountId, @RequestBody Map<String, Double> payload) {
        Double amount = payload.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "El monto de retiro debe ser superior a 0"));
        }

        return accountRepository.findById(accountId).map(account -> {
            if (account.getBalance() < amount) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Fondos insuficientes",
                    "accountId", account.getAccountId(),
                    "currentBalance", account.getBalance(),
                    "requestedAmount", amount
                ));
            }

            account.setBalance(account.getBalance() - amount);
            accountRepository.save(account);

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Retiro realizado con éxito");
            resp.put("accountId", account.getAccountId());
            resp.put("clientName", account.getClientName());
            resp.put("withdrawnAmount", amount);
            resp.put("newBalance", account.getBalance());

            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cuenta no encontrada: " + accountId)));
    }

    // 4. Realizar un Depósito / Abono de dinero
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<?> deposit(@PathVariable String accountId, @RequestBody Map<String, Double> payload) {
        Double amount = payload.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "El monto de abono debe ser superior a 0"));
        }

        return accountRepository.findById(accountId).map(account -> {
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Depósito acreditado con éxito");
            resp.put("accountId", account.getAccountId());
            resp.put("clientName", account.getClientName());
            resp.put("depositedAmount", amount);
            resp.put("newBalance", account.getBalance());

            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cuenta no encontrada: " + accountId)));
    }
}