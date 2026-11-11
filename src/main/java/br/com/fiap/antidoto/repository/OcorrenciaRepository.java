package br.com.fiap.antidoto.repository;

import br.com.fiap.antidoto.model.Ocorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    Page<Ocorrencia> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Só acha se for do dono. De outro usuário, vazio (vira 404). */
    Optional<Ocorrencia> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
