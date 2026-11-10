package br.com.fiap.rootfix.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "RF_PARTICIPACAO")
public class Participacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_COMUNIDADE", nullable = false)
    private Comunidade comunidade;

    @Column(name = "ENTROU_EM", nullable = false, updatable = false)
    private LocalDateTime entrouEm;

    public Participacao() {
    }

    public Participacao(Usuario usuario, Comunidade comunidade) {
        this.usuario = usuario;
        this.comunidade = comunidade;
    }

    @PrePersist
    void aoCriar() {
        if (entrouEm == null) {
            entrouEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Comunidade getComunidade() { return comunidade; }
    public LocalDateTime getEntrouEm() { return entrouEm; }
}
