package br.com.fiap.rootfix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.Observacao;

public interface ObservacaoRepository extends JpaRepository<Observacao, Long> {
    List<Observacao> findByProblemaIdOrderByCriadaEmAsc(Long problemaId);

    long countByProblemaId(Long problemaId);
}
