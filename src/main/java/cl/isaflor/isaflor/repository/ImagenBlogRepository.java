package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.ImagenBlog;

@Repository 
public interface ImagenBlogRepository extends JpaRepository<ImagenBlog, Integer>{

    
}