package br.com.fiap.antidoto.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nome")
    private String nome;
    @Column(name = "email")
    private String email;
    @Column(name = "senha_hash")
    private String senhaHash;
    @Column(name = "telefone")
    private String telefone;
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private Perfil perfil;
    @Column(name = "criado_em")
    private Instant criadoEm;

    public Usuario() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getSenhaHash() { return senhaHash; }

    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getTelefone() { return telefone; }

    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Perfil getPerfil() { return perfil; }

    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public Instant getCriadoEm() { return criadoEm; }

    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
