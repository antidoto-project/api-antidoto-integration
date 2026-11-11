package br.com.fiap.antidoto.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "ocorrencias")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Unidade escolhida no resultado ("Vou para esta unidade"); pode não ter. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id")
    private UnidadeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_animal")
    private TipoAnimal tipoAnimal;
    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_etaria")
    private FaixaEtaria faixaEtaria;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ocorrencia_sintomas", joinColumns = @JoinColumn(name = "ocorrencia_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "sintoma")
    private Set<Sintoma> sintomas = EnumSet.noneOf(Sintoma.class);

    @Column(name = "hora_picada")
    private Instant horaPicada;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia")
    private Urgencia urgencia;

    /** Motivos da urgência separados por ";" (MVP-01 §4.1); na resposta viram uma lista. */
    @Column(name = "motivos")
    private String motivos;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_resumo")
    private StatusResumo statusResumo;
    @Column(name = "resumo")
    private String resumo;
    @Column(name = "criado_em")
    private Instant criadoEm;

    public Ocorrencia() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public UnidadeSaude getUnidade() { return unidade; }

    public void setUnidade(UnidadeSaude unidade) { this.unidade = unidade; }

    public TipoAnimal getTipoAnimal() { return tipoAnimal; }

    public void setTipoAnimal(TipoAnimal tipoAnimal) { this.tipoAnimal = tipoAnimal; }

    public FaixaEtaria getFaixaEtaria() { return faixaEtaria; }

    public void setFaixaEtaria(FaixaEtaria faixaEtaria) { this.faixaEtaria = faixaEtaria; }

    public Set<Sintoma> getSintomas() { return sintomas; }

    public void setSintomas(Set<Sintoma> sintomas) { this.sintomas = sintomas; }

    public Instant getHoraPicada() { return horaPicada; }

    public void setHoraPicada(Instant horaPicada) { this.horaPicada = horaPicada; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Urgencia getUrgencia() { return urgencia; }

    public void setUrgencia(Urgencia urgencia) { this.urgencia = urgencia; }

    public String getMotivos() { return motivos; }

    public void setMotivos(String motivos) { this.motivos = motivos; }

    public StatusResumo getStatusResumo() { return statusResumo; }

    public void setStatusResumo(StatusResumo statusResumo) { this.statusResumo = statusResumo; }

    public String getResumo() { return resumo; }

    public void setResumo(String resumo) { this.resumo = resumo; }

    public Instant getCriadoEm() { return criadoEm; }

    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
