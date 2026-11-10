package br.com.fiap.rootfix.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.rootfix.entity.Apoio;

public interface ApoioRepository extends JpaRepository<Apoio, Long> {
    Optional<Apoio> findByUsuarioIdAndProblemaId(Long usuarioId, Long problemaId);

    boolean existsByUsuarioIdAndProblemaId(Long usuarioId, Long problemaId);

    long countByProblemaId(Long problemaId);

    List<Apoio> findByProblemaId(Long problemaId);

    @Query("select a.problema.id, count(a) from Apoio a where a.problema.comunidade.id = :comunidadeId group by a.problema.id")
    List<Object[]> contarPorProblemaDaComunidade(@Param("comunidadeId") Long comunidadeId);
}
