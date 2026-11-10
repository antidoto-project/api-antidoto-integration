package br.com.fiap.antidoto.repository;

import br.com.fiap.antidoto.model.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {
}
