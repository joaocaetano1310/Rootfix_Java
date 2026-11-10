package br.com.fiap.rootfix.entity;

import java.time.LocalDateTime;

import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "RF_NOTIFICACAO")
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROBLEMA")
    private Problema problema;

    @Column(name = "MENSAGEM", nullable = false, length = 300)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIORIDADE", nullable = false, length = 5)
    private PrioridadeNotificacao prioridade;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "LIDA", nullable = false)
    private boolean lida;

    @Column(name = "CRIADA_EM", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) {
            criadaEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Problema getProblema() { return problema; }
    public void setProblema(Problema problema) { this.problema = problema; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public PrioridadeNotificacao getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeNotificacao prioridade) { this.prioridade = prioridade; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
}
