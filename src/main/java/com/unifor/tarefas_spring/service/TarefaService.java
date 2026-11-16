package com.unifor.tarefas_spring.service;

import com.unifor.tarefas_spring.exception.DadosInvalidosException;
import com.unifor.tarefas_spring.exception.TarefaNaoEncontradaException;
import com.unifor.tarefas_spring.model.Prioridade;
import com.unifor.tarefas_spring.model.Status;
import com.unifor.tarefas_spring.model.Tarefa;
import com.unifor.tarefas_spring.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository repository;

    // Injeção pelo construtor: o Spring entrega o bean TarefaRepository que ele mesmo criou
    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listar(Status status, Prioridade prioridade) {
        return repository.listar(status, prioridade);
    }

    @Transactional(readOnly = true)
    public Tarefa buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    @Transactional
    public Tarefa criar(Tarefa nova) {
        if (nova.getTitulo() == null || nova.getTitulo().isBlank()) {
            throw new DadosInvalidosException("O campo 'titulo' é obrigatório");
        }
        validarTamanhos(nova);
        // O id é gerado pelo banco. Se vier no corpo, é descartado (senão o save faria um update)
        nova.setId(null);
        if (nova.getStatus() == null) {
            nova.setStatus(Status.PENDENTE);
        }
        if (nova.getPrioridade() == null) {
            nova.setPrioridade(Prioridade.MEDIA);
        }
        return repository.save(nova);
    }

    /**
     * Altera só os campos enviados (os que vierem nulos continuam iguais).
     * Não precisa chamar save: a tarefa buscada fica gerenciada pelo Hibernate dentro da transação,
     * e as alterações são gravadas com um UPDATE no commit (dirty checking).
     */
    @Transactional
    public Tarefa editar(Long id, Tarefa alteracoes) {
        if (alteracoes.getTitulo() != null && alteracoes.getTitulo().isBlank()) {
            throw new DadosInvalidosException("O campo 'titulo' não pode ficar vazio");
        }
        validarTamanhos(alteracoes);
        Tarefa atual = buscar(id);
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
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new TarefaNaoEncontradaException(id);
        }
        repository.deleteById(id);
    }

    /** Evita que o banco recuse o INSERT/UPDATE (e a API devolva 500) por texto maior que a coluna. */
    private void validarTamanhos(Tarefa tarefa) {
        if (tarefa.getTitulo() != null && tarefa.getTitulo().length() > Tarefa.TAMANHO_TITULO) {
            throw new DadosInvalidosException(
                    "O campo 'titulo' deve ter no máximo " + Tarefa.TAMANHO_TITULO + " caracteres");
        }
        if (tarefa.getDescricao() != null && tarefa.getDescricao().length() > Tarefa.TAMANHO_DESCRICAO) {
            throw new DadosInvalidosException(
                    "O campo 'descricao' deve ter no máximo " + Tarefa.TAMANHO_DESCRICAO + " caracteres");
        }
    }
}
