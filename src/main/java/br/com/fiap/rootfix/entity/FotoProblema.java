package br.com.fiap.rootfix.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "RF_FOTO_PROBLEMA")
public class FotoProblema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PROBLEMA", nullable = false)
    private Problema problema;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DADOS", nullable = false)
    private byte[] dados;

    @Column(name = "CONTENT_TYPE", nullable = false, length = 50)
    private String contentType;

    @Column(name = "TAMANHO_BYTES", nullable = false)
    private Integer tamanhoBytes;

    public Long getId() { return id; }

    public Problema getProblema() { return problema; }
    public void setProblema(Problema problema) { this.problema = problema; }

    public byte[] getDados() { return dados; }
    public void setDados(byte[] dados) { this.dados = dados; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Integer getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(Integer tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }
}
