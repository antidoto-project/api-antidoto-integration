package br.com.fiap.antidoto.repository;

import br.com.fiap.antidoto.model.UnidadeSaude;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnidadeSaudeRepository extends JpaRepository<UnidadeSaude, Long> {

    /** RN-S1: só as ativas. */
    List<UnidadeSaude> findByAtivaTrue();

    Page<UnidadeSaude> findByAtivaTrue(Pageable pageable);

    Optional<UnidadeSaude> findByIdAndAtivaTrue(Long id);

    boolean existsByNome(String nome);
}
