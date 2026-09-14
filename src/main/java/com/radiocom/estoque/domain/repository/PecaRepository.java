package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.Peca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PecaRepository extends JpaRepository<Peca, UUID> {

    boolean existsByCodigoIgnoreCase(String codigo);

    /**
     * Listagem combinada: emFalta/estoqueBaixo/critico/modeloCompativelId são
     * filtros independentes e opcionais (false/null = sem restrição), podendo
     * ser combinados livremente. "critico" é a união de emFalta OU
     * estoqueBaixo (usado pela aba "Estoque crítico" do front).
     */
    @Query("SELECT DISTINCT p FROM Peca p LEFT JOIN p.modelosCompativeis m WHERE "
            + "(:modeloCompativelId IS NULL OR m.id = :modeloCompativelId) AND "
            + "(:emFalta = FALSE OR p.quantidadeDisponivel = 0) AND "
            + "(:estoqueBaixo = FALSE OR (p.quantidadeMinima IS NOT NULL "
            + "   AND p.quantidadeDisponivel <= p.quantidadeMinima AND p.quantidadeDisponivel > 0)) AND "
            + "(:critico = FALSE OR p.quantidadeDisponivel = 0 OR (p.quantidadeMinima IS NOT NULL "
            + "   AND p.quantidadeDisponivel <= p.quantidadeMinima AND p.quantidadeDisponivel > 0))")
    Page<Peca> buscar(
            @Param("modeloCompativelId") UUID modeloCompativelId,
            @Param("emFalta") boolean emFalta,
            @Param("estoqueBaixo") boolean estoqueBaixo,
            @Param("critico") boolean critico,
            Pageable pageable);
}
