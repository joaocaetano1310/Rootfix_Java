package br.com.fiap.rootfix.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.FotoProblema;

public interface FotoProblemaRepository extends JpaRepository<FotoProblema, Long> {
    boolean existsByProblemaId(Long problemaId);

    Optional<FotoProblema> findFirstByProblemaId(Long problemaId);
}
