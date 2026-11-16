package com.unifor.tarefas_spring.repository;

import com.unifor.tarefas_spring.model.Prioridade;
import com.unifor.tarefas_spring.model.Status;
import com.unifor.tarefas_spring.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositório JPA: o Spring Data gera a implementação em tempo de execução.
 * O CRUD básico (findById, save, existsById, deleteById...) vem de {@link JpaRepository}.
 */
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    /** Listagem com filtros opcionais: o filtro que vier nulo não é aplicado. */
    @Query("""
            select t from Tarefa t
            where (:status is null or t.status = :status)
              and (:prioridade is null or t.prioridade = :prioridade)
            order by t.id
            """)
    List<Tarefa> listar(@Param("status") Status status, @Param("prioridade") Prioridade prioridade);
}
