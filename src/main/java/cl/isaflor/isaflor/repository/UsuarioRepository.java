package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.Usuario;

@Repository 
public interface UsuarioRepository extends JpaRepository <Usuario, Integer>{
    
}
