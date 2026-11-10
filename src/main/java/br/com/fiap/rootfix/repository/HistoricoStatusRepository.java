package br.com.fiap.rootfix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.HistoricoStatus;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {
}
