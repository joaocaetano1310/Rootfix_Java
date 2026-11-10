package br.com.fiap.rootfix.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "RF_HISTORICO_STATUS")
public class HistoricoStatus {
    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PROBLEMA", nullable = false)
    private Problema problema;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_ANTERIOR", length = 20)
    private StatusProblema statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_NOVO", nullable = false, length = 20)
    private StatusProblema statusNovo;

    @Column(name = "DATA_HORA", nullable = false)
    private LocalDateTime dataHora;

    public Long getId() { return id; }
    public Problema getProblema() { return problema; }
    public StatusProblema getStatusAnterior() { return statusAnterior; }
    public StatusProblema getStatusNovo() { return statusNovo; }
    public LocalDateTime getDataHora() { return dataHora; }
}
