package br.com.fiap.antidoto.repository;

import br.com.fiap.antidoto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
