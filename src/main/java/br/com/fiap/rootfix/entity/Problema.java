package br.com.fiap.rootfix.entity;

import java.math.BigDecimal;
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
@Table(name = "RF_PROBLEMA")
public class Problema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITULO", nullable = false, length = 120)
    private String titulo;

    @Column(name = "DESCRICAO", nullable = false, length = 1000)
    private String descricao;

    @Column(name = "LATITUDE", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_AUTOR", nullable = false)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_COMUNIDADE", nullable = false)
    private Comunidade comunidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CATEGORIA")
    private Categoria categoria;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "ADOTADO", nullable = false)
    private boolean adotado;

    @Column(name = "ADOTADO_EM")
    private LocalDateTime adotadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20)
    private StatusProblema status;

    @Column(name = "PRIORIDADE", length = 20)
    private String prioridade;

    @Enumerated(EnumType.STRING)
    @Column(name = "GRAVIDADE_IA", length = 10)
    private Gravidade gravidadeIa;

    @Column(name = "JUSTIFICATIVA_IA", length = 500)
    private String justificativaIa;

    @Column(name = "CRIADO_EM", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }

    public Comunidade getComunidade() { return comunidade; }
    public void setComunidade(Comunidade comunidade) { this.comunidade = comunidade; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public boolean isAdotado() { return adotado; }
    public void setAdotado(boolean adotado) { this.adotado = adotado; }

    public LocalDateTime getAdotadoEm() { return adotadoEm; }
    public void setAdotadoEm(LocalDateTime adotadoEm) { this.adotadoEm = adotadoEm; }

    public StatusProblema getStatus() { return status; }
    public void setStatus(StatusProblema status) { this.status = status; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public Gravidade getGravidadeIa() { return gravidadeIa; }
    public void setGravidadeIa(Gravidade gravidadeIa) { this.gravidadeIa = gravidadeIa; }

    public String getJustificativaIa() { return justificativaIa; }
    public void setJustificativaIa(String justificativaIa) { this.justificativaIa = justificativaIa; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}
