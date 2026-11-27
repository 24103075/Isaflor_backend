package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.Video;

@Repository 
public interface VideoRepository extends JpaRepository<Video, Integer>{

    boolean existsByBlog_IdBlog(Integer idBlog);

    List<Video> findAllByOrderByIdVideoAsc();

    List<Video> findByBlog_EstadoOrderByIdVideoAsc(String estado);
}
