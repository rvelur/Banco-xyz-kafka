package cl.duoc.customer.config;

import cl.duoc.customer.entity.Customer;
import cl.duoc.customer.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(CustomerRepository repository) {
        return args -> {
            repository.save(new Customer("11111111-1", "Carlos Mendoza", "carlos.mendoza@bancoxyz.cl", "+56911223344", "PREMIUM", "ACTIVO"));
            repository.save(new Customer("22222222-2", "Beatriz Soto", "beatriz.soto@bancoxyz.cl", "+56922334455", "ESTANDAR", "ACTIVO"));
            repository.save(new Customer("33333333-3", "Juan Perez", "juan.perez@bancoxyz.cl", "+56933445566", "PREMIUM", "ACTIVO"));
        };
    }
}