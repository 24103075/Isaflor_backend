package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.Producto;

@Repository 
public interface ProductoRepository extends JpaRepository<Producto, Integer>{

    
}
