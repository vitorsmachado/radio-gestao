package com.radiocom.configuracao.domain.repository;

import com.radiocom.configuracao.domain.model.Configuracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracaoRepository extends JpaRepository<Configuracao, UUID> {

    /** Sempre há uma única linha (criada pela migration) — LIMIT 1 implícito evita erro se algum dia houver mais de uma. */
    Optional<Configuracao> findFirstByOrderByDataCriacaoAsc();
}
