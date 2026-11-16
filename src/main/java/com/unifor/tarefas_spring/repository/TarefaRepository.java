package com.unifor.tarefas_spring.repository;

import com.unifor.tarefas_spring.model.Prioridade;
import com.unifor.tarefas_spring.model.Status;
import com.unifor.tarefas_spring.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @Query("""
            select t from Tarefa t
            left join fetch t.categoria
            where (:status is null or t.status = :status)
              and (:prioridade is null or t.prioridade = :prioridade)
            order by t.id
            """)
    List<Tarefa> listar(@Param("status") Status status, @Param("prioridade") Prioridade prioridade);
}
