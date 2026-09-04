package com.radiocom.ordemservico.domain.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
public class NumeroOSGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String PREFIXO = "OS";

    /**
     * Gera o próximo número de OS no formato OS-YYYY-NNNN de forma atômica.
     *
     * Usa uma sequence PostgreSQL por ano (os_seq_YYYY) para garantir
     * unicidade sem race condition em ambiente concorrente.
     * A sequence é criada automaticamente na primeira OS do ano.
     *
     * Executa em REQUIRES_NEW para que o nextval seja confirmado mesmo que
     * a transação chamadora faça rollback — gaps na numeração são aceitáveis
     * e preferíveis a colisões de número duplicado.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String gerarNumero() {
        int ano = LocalDate.now().getYear();
        String nomeSequence = "os_seq_" + ano;

        entityManager.createNativeQuery(
                "CREATE SEQUENCE IF NOT EXISTS " + nomeSequence
                        + " START WITH 1 INCREMENT BY 1 NO CYCLE"
        ).executeUpdate();

        Number nextVal = (Number) entityManager
                .createNativeQuery("SELECT nextval('" + nomeSequence + "')")
                .getSingleResult();

        String numero = String.format("%s-%d-%04d", PREFIXO, ano, nextVal.longValue());
        log.debug("Número OS gerado: {}", numero);
        return numero;
    }
}
