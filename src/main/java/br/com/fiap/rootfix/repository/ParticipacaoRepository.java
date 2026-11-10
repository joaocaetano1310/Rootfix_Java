package br.com.fiap.rootfix.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.Participacao;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {
    boolean existsByUsuarioIdAndComunidadeId(Long usuarioId, Long comunidadeId);

    Optional<Participacao> findByUsuarioIdAndComunidadeId(Long usuarioId, Long comunidadeId);

    List<Participacao> findByUsuarioIdOrderByEntrouEmDesc(Long usuarioId);

    long countByComunidadeId(Long comunidadeId);

    List<Participacao> findByComunidadeId(Long comunidadeId);
}
