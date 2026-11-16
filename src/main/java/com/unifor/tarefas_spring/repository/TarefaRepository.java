package com.unifor.tarefas_spring.repository;

import com.unifor.tarefas_spring.model.Prioridade;
import com.unifor.tarefas_spring.model.Status;
import com.unifor.tarefas_spring.model.Tarefa;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TarefaRepository {
    private final Map<Long, Tarefa> tarefas = new ConcurrentHashMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public List<Tarefa> listar(Status status, Prioridade prioridade) {
        return tarefas.values().stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> prioridade == null || t.getPrioridade() == prioridade)
                .sorted(Comparator.comparing(Tarefa::getId))
                .toList();
    }

    public Optional<Tarefa> buscarPorId(Long id) {
        return Optional.ofNullable(tarefas.get(id));
    }

    public Tarefa salvar(Tarefa tarefa) {
        tarefa.setId(proximoId.getAndIncrement());
        tarefas.put(tarefa.getId(), tarefa);
        return tarefa;
    }

    public Optional<Tarefa> atualizar(Long id, Tarefa alteracoes) {
        return Optional.ofNullable(tarefas.computeIfPresent(id, (chave, atual) -> {
            if (alteracoes.getTitulo() != null) {
                atual.setTitulo(alteracoes.getTitulo());
            }
            if (alteracoes.getDescricao() != null) {
                atual.setDescricao(alteracoes.getDescricao());
            }
            if (alteracoes.getStatus() != null) {
                atual.setStatus(alteracoes.getStatus());
            }
            if (alteracoes.getPrioridade() != null) {
                atual.setPrioridade(alteracoes.getPrioridade());
            }
            return atual;
        }));
    }

    public boolean remover(Long id) {
        return tarefas.remove(id) != null;
    }
}
