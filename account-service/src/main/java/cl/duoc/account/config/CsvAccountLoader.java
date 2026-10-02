package cl.duoc.account.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import cl.duoc.account.model.Account;
import cl.duoc.account.repository.AccountRepository;

@Component
public class CsvAccountLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CsvAccountLoader.class);

    private final AccountRepository accountRepository;
    private final ResourceLoader resourceLoader;

    public CsvAccountLoader(AccountRepository accountRepository, ResourceLoader resourceLoader) {
        this.accountRepository = accountRepository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) {
        log.info("==> Iniciando carga y sanitización de cuentas bancarias desde intereses.csv...");

        Resource resource = resourceLoader.getResource("classpath:data/intereses.csv");
        if (!resource.exists()) {
            log.warn("Archivo intereses.csv no encontrado en classpath:data/. Inicializando cuentas default.");
            cargarCuentasDefault();
            return;
        }

        Set<String> idsProcesados = new HashSet<>();
        int cargadas = 0;
        int omitidas = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean primerLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primerLinea) {
                    primerLinea = false;
                    continue; // Saltar encabezado: cuenta_id,nombre,saldo,edad,tipo
                }

                if (line.trim().isEmpty()) continue;

                // Estructura: cuenta_id,nombre,saldo,edad,tipo
                String[] cols = line.split(",", -1);

                if (cols.length < 5) {
                    omitidas++;
                    continue;
                }

                String cuentaId = cols[0].trim();
                String nombre = cols[1].trim();
                String saldoStr = cols[2].trim();
                String edadStr = cols[3].trim();
                String tipo = cols[4].trim();

                // Regla 1: Validar ID y evitar duplicados
                if (cuentaId.isEmpty() || idsProcesados.contains(cuentaId)) {
                    omitidas++;
                    continue;
                }

                // Regla 2: Descartar registros con nombre "Unknown" o vacío
                if (nombre.isEmpty() || nombre.equalsIgnoreCase("Unknown")) {
                    omitidas++;
                    continue;
                }

                // Regla 3: Validar saldo numérico y no nulo
                if (saldoStr.isEmpty()) {
                    omitidas++;
                    continue;
                }

                double saldo;
                try {
                    saldo = Double.parseDouble(saldoStr);
                    if (saldo < 0) {
                        omitidas++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    omitidas++;
                    continue;
                }

                // Regla 4: Sanitizar edad
                Integer edad = null;
                try {
                    if (!edadStr.isEmpty()) {
                        int parsedEdad = Integer.parseInt(edadStr);
                        if (parsedEdad > 0 && parsedEdad <= 120) {
                            edad = parsedEdad;
                        }
                    }
                } catch (NumberFormatException ignored) {}

                // Regla 5: Sanitizar tipo de producto
                if (tipo.equals("-1") || tipo.isEmpty()) {
                    tipo = "cuenta_corriente";
                }

                // Guardar cuenta en base de datos H2
                Account account = new Account(
                    cuentaId,
                    nombre,
                    saldo,
                    edad,
                    tipo,
                    "ACTIVE"
                );

                accountRepository.save(account);
                idsProcesados.add(cuentaId);
                cargadas++;
            }

            log.info("==> Carga legacy completada: {} cuentas válidas ingresadas a la BD, {} registros sucios omitidos.", cargadas, omitidas);

            // Aseguramos que la cuenta de prueba ACC-100 exista para compatibilidad de pruebas previas
            if (!accountRepository.existsById("ACC-100")) {
                accountRepository.save(new Account("ACC-100", "Carlos Perez Gonzalez", 2500000.0, 35, "cuenta_corriente", "ACTIVE"));
                log.info("==> Cuenta ACC-100 agregada satisfactoriamente.");
            }

        } catch (Exception ex) {
            log.error("Error al procesar intereses.csv: {}", ex.getMessage());
            cargarCuentasDefault();
        }
    }

    private void cargarCuentasDefault() {
        if (accountRepository.count() == 0) {
            accountRepository.save(new Account("ACC-100", "Carlos Perez Gonzalez", 2500000.0, 35, "cuenta_corriente", "ACTIVE"));
            accountRepository.save(new Account("137", "Bob Johnson", 7000.0, 45, "prestamo", "ACTIVE"));
            accountRepository.save(new Account("133", "Alice Brown", 12000.0, 100, "hipoteca", "ACTIVE"));
            accountRepository.save(new Account("106", "Jane Smith", 12000.0, 40, "prestamo", "ACTIVE"));
        }
    }
}