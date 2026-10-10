package cl.duoc.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.batch.entity.EstadoCuentaAnual;

public interface EstadoCuentaAnualRepository extends JpaRepository<EstadoCuentaAnual, Long> {
}