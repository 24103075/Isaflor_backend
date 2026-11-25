package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.Blog;

@Repository 
public interface BlogRepository extends JpaRepository<Blog, Integer> {

    
}
