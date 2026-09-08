package com.radiocom.orcamento.domain.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
public class NumeroOrcamentoGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String PREFIXO = "ORC";

    /**
     * Gera o próximo número de orçamento no formato ORC-YYYY-NNNN de forma
     * atômica, com o mesmo mecanismo de sequence por ano usado em
     * {@link com.radiocom.ordemservico.domain.service.NumeroOSGenerator}.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String gerarNumero() {
        int ano = LocalDate.now().getYear();
        String nomeSequence = "orc_seq_" + ano;

        entityManager.createNativeQuery(
                "CREATE SEQUENCE IF NOT EXISTS " + nomeSequence
                        + " START WITH 1 INCREMENT BY 1 NO CYCLE"
        ).executeUpdate();

        Number nextVal = (Number) entityManager
                .createNativeQuery("SELECT nextval('" + nomeSequence + "')")
                .getSingleResult();

        String numero = String.format("%s-%d-%04d", PREFIXO, ano, nextVal.longValue());
        log.debug("Número de orçamento gerado: {}", numero);
        return numero;
    }
}
