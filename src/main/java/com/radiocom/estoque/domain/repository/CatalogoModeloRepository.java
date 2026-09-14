package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.CatalogoModelo;
import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CatalogoModeloRepository extends JpaRepository<CatalogoModelo, UUID> {

    Optional<CatalogoModelo> findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(
            TipoItem tipoItem, String marca, String modelo);

    boolean existsByReferenciaIgnoreCase(String referencia);        

    /**
     * Listagem combinada: todos os filtros são opcionais (null = sem restrição).
     * A busca cobre marca, modelo e descrição — o filtro por marca/modelo isolado
     * do sistema antigo não cobria descrição.
     */
    @Query("SELECT c FROM CatalogoModelo c WHERE "
            + "(:tipoItem IS NULL OR c.tipoItem = :tipoItem) AND "
            + "(:status IS NULL OR c.status = :status) AND "
            + "(:busca IS NULL OR "
            + "   LOWER(c.marca) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "   LOWER(c.modelo) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "   LOWER(c.descricao) LIKE LOWER(CONCAT('%', :busca, '%')))")
    Page<CatalogoModelo> buscar(
            @Param("busca") String busca,
            @Param("tipoItem") TipoItem tipoItem,
            @Param("status") StatusItem status,
            Pageable pageable);

    /** Marcas distintas já cadastradas — usado pra sugerir marca no cadastro de um novo item. */
    @Query("SELECT DISTINCT c.marca FROM CatalogoModelo c ORDER BY c.marca")
    List<String> listarMarcas();
}
