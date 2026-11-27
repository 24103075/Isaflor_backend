package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.Blog;

@Repository 
public interface BlogRepository extends JpaRepository<Blog, Integer> {

    List<Blog> findAllByOrderByFechaPublicacionDesc();

    List<Blog> findByEstadoOrderByFechaPublicacionDesc(String estado);
}
