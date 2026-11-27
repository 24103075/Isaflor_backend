package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.ImagenBlog;

@Repository 
public interface ImagenBlogRepository extends JpaRepository<ImagenBlog, Integer>{

    boolean existsByBlog_IdBlog(Integer idBlog);

    List<ImagenBlog> findAllByOrderByIdImagenBlogAsc();

    List<ImagenBlog> findByBlog_EstadoOrderByIdImagenBlogAsc(String estado);
}