package cl.duoc.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.batch.entity.ReporteTransaccionDiaria;

@Repository
public interface ReporteTransaccionDiariaRepository extends JpaRepository<ReporteTransaccionDiaria, Long> {
}