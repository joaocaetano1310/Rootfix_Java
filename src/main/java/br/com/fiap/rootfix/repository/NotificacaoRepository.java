package br.com.fiap.rootfix.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    Page<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId, Pageable paginacao);

    Optional<Notificacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    long deleteByUsuarioIdAndLida(Long usuarioId, boolean lida);
}
