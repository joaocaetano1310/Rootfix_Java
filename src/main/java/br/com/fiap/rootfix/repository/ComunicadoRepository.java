package br.com.fiap.rootfix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.Comunicado;

public interface ComunicadoRepository extends JpaRepository<Comunicado, Long> {
    Page<Comunicado> findByComunidadeIdOrderByDestacadoDescCriadoEmDesc(Long comunidadeId, Pageable pageable);
}
