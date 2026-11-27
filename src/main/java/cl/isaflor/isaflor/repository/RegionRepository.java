package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.Region;

@Repository 
public interface RegionRepository extends JpaRepository<Region, Integer>{

    boolean existsByNombreRegionIgnoreCase(String nombreRegion);

    boolean existsByNombreRegionIgnoreCaseAndIdRegionNot(
            String nombreRegion,
            Integer idRegion);
}

