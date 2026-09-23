package com.radiocom.ordemservico.sugestao.domain.repository;

import com.radiocom.ordemservico.sugestao.domain.model.SugestaoTexto;
import com.radiocom.ordemservico.sugestao.domain.model.enums.CampoSugestao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SugestaoTextoRepository extends JpaRepository<SugestaoTexto, UUID> {

    Optional<SugestaoTexto> findByCampoAndValorIgnoreCase(CampoSugestao campo, String valor);

    @Query("SELECT s FROM SugestaoTexto s WHERE s.campo = :campo "
            + "AND (:busca IS NULL OR LOWER(s.valor) LIKE LOWER(CONCAT('%', :busca, '%'))) "
            + "ORDER BY s.contagemUso DESC, s.dataAtualizacao DESC")
    List<SugestaoTexto> buscar(@Param("campo") CampoSugestao campo, @Param("busca") String busca, Pageable pageable);
}
