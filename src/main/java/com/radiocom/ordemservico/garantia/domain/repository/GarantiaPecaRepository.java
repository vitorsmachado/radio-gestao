package com.radiocom.ordemservico.garantia.domain.repository;

import com.radiocom.ordemservico.garantia.domain.model.GarantiaPeca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GarantiaPecaRepository extends JpaRepository<GarantiaPeca, UUID> {

    List<GarantiaPeca> findByItemEstoqueIdAndDataFimGreaterThanEqual(UUID itemEstoqueId, LocalDate data);
}
