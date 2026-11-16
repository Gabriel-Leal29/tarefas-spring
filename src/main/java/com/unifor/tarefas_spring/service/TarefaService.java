package com.unifor.tarefas_spring.service;

import com.unifor.tarefas_spring.exception.DadosInvalidosException;
import com.unifor.tarefas_spring.exception.TarefaNaoEncontradaException;
import com.unifor.tarefas_spring.model.Prioridade;
import com.unifor.tarefas_spring.model.Status;
import com.unifor.tarefas_spring.model.Tarefa;
import com.unifor.tarefas_spring.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository repository;

    // Injeção pelo construtor: o Spring entrega o bean TarefaRepository que ele mesmo criou
    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    public List<Tarefa> listar(Status status, Prioridade prioridade) {
        return repository.listar(status, prioridade);
    }

    public Tarefa buscar(Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    public Tarefa criar(Tarefa nova) {
        if (nova.getTitulo() == null || nova.getTitulo().isBlank()) {
            throw new DadosInvalidosException("O campo 'titulo' é obrigatório");
        }
        if (nova.getStatus() == null) {
            nova.setStatus(Status.PENDENTE);
        }
        if (nova.getPrioridade() == null) {
            nova.setPrioridade(Prioridade.MEDIA);
        }
        return repository.salvar(nova);
    }

    /** Altera só os campos enviados (os que vierem nulos continuam iguais). */
    public Tarefa editar(Long id, Tarefa alteracoes) {
        if (alteracoes.getTitulo() != null && alteracoes.getTitulo().isBlank()) {
            throw new DadosInvalidosException("O campo 'titulo' não pode ficar vazio");
        }
        return repository.atualizar(id, alteracoes).orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    public void excluir(Long id) {
        if (!repository.remover(id)) {
            throw new TarefaNaoEncontradaException(id);
        }
    }
}
