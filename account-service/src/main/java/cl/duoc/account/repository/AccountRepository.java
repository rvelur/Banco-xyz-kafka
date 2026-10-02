package cl.duoc.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.account.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}