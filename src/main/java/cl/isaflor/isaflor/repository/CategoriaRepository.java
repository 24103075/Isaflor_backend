package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.Categoria;

@Repository 
public interface CategoriaRepository extends JpaRepository<Categoria, Integer>{

    boolean existsByNombreCategoriaIgnoreCase(String nombreCategoria);

    boolean existsByNombreCategoriaIgnoreCaseAndIdCategoriaNot(
            String nombreCategoria,
            Integer idCategoria);
}
