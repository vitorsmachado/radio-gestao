package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.CatalogoModelo;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CatalogoModeloRepository extends JpaRepository<CatalogoModelo, UUID> {

    Optional<CatalogoModelo> findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(
            TipoItem tipoItem, String marca, String modelo);

    List<CatalogoModelo> findByTipoItem(TipoItem tipoItem);

    Page<CatalogoModelo> findByMarcaContainingIgnoreCaseOrModeloContainingIgnoreCase(
            String marca, String modelo, Pageable pageable);
}
