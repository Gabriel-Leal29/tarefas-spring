package com.unifor.tarefas_spring.service;

import com.unifor.tarefas_spring.exception.DadosInvalidosException;
import com.unifor.tarefas_spring.exception.TarefaNaoEncontradaException;
import com.unifor.tarefas_spring.model.Categoria;
import com.unifor.tarefas_spring.model.Prioridade;
import com.unifor.tarefas_spring.model.Status;
import com.unifor.tarefas_spring.model.Tarefa;
import com.unifor.tarefas_spring.repository.CategoriaRepository;
import com.unifor.tarefas_spring.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository repository;
    private final CategoriaRepository categoriaRepository;

    public TarefaService(TarefaRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
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
        nova.setId(null);
        if (nova.getStatus() == null) {
            nova.setStatus(Status.PENDENTE);
        }
        if (nova.getPrioridade() == null) {
            nova.setPrioridade(Prioridade.MEDIA);
        }
        if (nova.getCategoria() != null) {
            nova.setCategoria(buscarCategoria(nova.getCategoria()));
        }
        return repository.save(nova);
    }

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
        if (alteracoes.getCategoria() != null) {
            atual.setCategoria(buscarCategoria(alteracoes.getCategoria()));
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

    private Categoria buscarCategoria(Categoria enviada) {
        Long id = enviada.getId();
        if (id == null) {
            throw new DadosInvalidosException("Informe o 'id' da categoria");
        }
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new DadosInvalidosException("A categoria " + id + " não existe"));
    }

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
