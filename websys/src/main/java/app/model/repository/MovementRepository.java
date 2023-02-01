package app.model.repository;

import java.util.List;

import app.model.entity.Movement;
import app.model.entity.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementRepository extends JpaRepository<Movement, Integer> {

    List<Movement> findByNfeChave(String chave);
    List<Movement> findByTypeAndOrcamento(MovementType mtype, Boolean orcamento);
    List<Movement> findByType(MovementType mtype);
    List<Movement> findByCadastroId(Integer cadastroId);

}