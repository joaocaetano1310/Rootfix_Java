package br.com.fiap.rootfix.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.Problema;

public interface ProblemaRepository extends JpaRepository<Problema, Long> {
    boolean existsByComunidadeId(Long comunidadeId);

    Page<Problema> findByComunidadeId(Long comunidadeId, Pageable paginacao);

    List<Problema> findByComunidadeId(Long comunidadeId);

    Page<Problema> findByComunidadeIdAndAdotado(Long comunidadeId, boolean adotado, Pageable paginacao);

    List<Problema> findByComunidadeIdInAndAdotado(Collection<Long> comunidadeIds, boolean adotado);
}
