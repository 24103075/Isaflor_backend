package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.TipoDocumento;

@Repository 
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {

}