package br.com.fiap.antidoto.repository;

import br.com.fiap.antidoto.model.Avistamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvistamentoRepository extends JpaRepository<Avistamento, Long> {

    Page<Avistamento> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** RN-G5: só acha se for do dono. De outro usuário, vazio (vira 404). */
    Optional<Avistamento> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
