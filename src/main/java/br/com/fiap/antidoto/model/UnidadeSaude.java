package br.com.fiap.antidoto.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "unidades_saude")
public class UnidadeSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nome")
    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoUnidade tipo;
    @Column(name = "endereco")
    private String endereco;
    @Column(name = "telefone")
    private String telefone;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "atende_24h")
    private Boolean atende24h;
    @Column(name = "ativa")
    private Boolean ativa = true;

    /** Soros disponíveis na unidade (tabela unidade_soros). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "unidade_soros", joinColumns = @JoinColumn(name = "unidade_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "soro")
    private Set<TipoSoro> soros = EnumSet.noneOf(TipoSoro.class);

    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    public UnidadeSaude() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public TipoUnidade getTipo() { return tipo; }

    public void setTipo(TipoUnidade tipo) { this.tipo = tipo; }

    public String getEndereco() { return endereco; }

    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }

    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getAtende24h() { return atende24h; }

    public void setAtende24h(Boolean atende24h) { this.atende24h = atende24h; }

    public Boolean getAtiva() { return ativa; }

    public void setAtiva(Boolean ativa) { this.ativa = ativa; }

    public Set<TipoSoro> getSoros() { return soros; }

    public void setSoros(Set<TipoSoro> soros) { this.soros = soros; }

    public Instant getAtualizadoEm() { return atualizadoEm; }

    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
