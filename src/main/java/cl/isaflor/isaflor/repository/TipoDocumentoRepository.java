package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.TipoDocumento;

@Repository 
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {

    boolean existsByNombreDocumentoIgnoreCase(String nombreDocumento);

    boolean existsByNombreDocumentoIgnoreCaseAndIdTipoDocumentoNot(
            String nombreDocumento,
            Integer idTipoDocumento);
}