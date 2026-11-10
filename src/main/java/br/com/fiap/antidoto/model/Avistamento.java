package br.com.fiap.antidoto.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "avistamentos")
public class Avistamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** O dono (RN-G5): só ele vê, edita e exclui. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_animal")
    private TipoAnimal tipoAnimal;

    /** Coluna "local_avist": LOCAL é palavra reservada em alguns bancos. No JSON, "local". */
    @Enumerated(EnumType.STRING)
    @Column(name = "local_avist")
    private LocalAvistamento local;

    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "data_hora")
    private Instant dataHora;
    @Column(name = "observacao")
    private String observacao;
    @Column(name = "criado_em")
    private Instant criadoEm;
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    public Avistamento() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoAnimal getTipoAnimal() { return tipoAnimal; }

    public void setTipoAnimal(TipoAnimal tipoAnimal) { this.tipoAnimal = tipoAnimal; }

    public LocalAvistamento getLocal() { return local; }

    public void setLocal(LocalAvistamento local) { this.local = local; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Instant getDataHora() { return dataHora; }

    public void setDataHora(Instant dataHora) { this.dataHora = dataHora; }

    public String getObservacao() { return observacao; }

    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Instant getCriadoEm() { return criadoEm; }

    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }

    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
