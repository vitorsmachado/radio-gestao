package com.radiocom.cliente.domain.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gera o próximo número de identificação do cliente de forma atômica, via
 * sequence PostgreSQL, evitando colisão em ambiente concorrente.
 *
 * Executa em REQUIRES_NEW para que o nextval seja confirmado mesmo que a
 * transação chamadora faça rollback — gaps na numeração são aceitáveis e
 * preferíveis a colisões de número duplicado.
 */
@Service
@Slf4j
public class NumeroClienteGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer gerarNumero() {
        Number nextVal = (Number) entityManager
                .createNativeQuery("SELECT nextval('cliente_numero_seq')")
                .getSingleResult();
        Integer numero = nextVal.intValue();
        log.debug("Número de identificação de cliente gerado: {}", numero);
        return numero;
    }
}
