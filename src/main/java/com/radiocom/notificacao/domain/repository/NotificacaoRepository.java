package com.radiocom.notificacao.domain.repository;

import com.radiocom.notificacao.domain.model.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    Page<Notificacao> findByLidaOrderByDataCriacaoDesc(boolean lida, Pageable pageable);

    Page<Notificacao> findAllByOrderByDataCriacaoDesc(Pageable pageable);

    long countByLidaFalse();
}
