package br.com.fiap.antidoto.repository;

import br.com.fiap.antidoto.model.Avistamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvistamentoRepository extends JpaRepository<Avistamento, Long> {
}
