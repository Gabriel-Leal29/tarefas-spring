package com.unifor.tarefas_spring.service;

import com.unifor.tarefas_spring.exception.CategoriaNaoEncontradaException;
import com.unifor.tarefas_spring.exception.ConflitoException;
import com.unifor.tarefas_spring.exception.DadosInvalidosException;
import com.unifor.tarefas_spring.model.Categoria;
import com.unifor.tarefas_spring.model.Tarefa;
import com.unifor.tarefas_spring.repository.CategoriaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return repository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Categoria buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new CategoriaNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listarTarefas(Long id) {
        return List.copyOf(buscar(id).getTarefas());
    }

    @Transactional
    public Categoria criar(Categoria nova) {
        String nome = validarNome(nova.getNome());
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe uma categoria com o nome '" + nome + "'");
        }
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return repository.save(categoria);
    }

    @Transactional
    public Categoria editar(Long id, Categoria alteracoes) {
        String nome = validarNome(alteracoes.getNome());
        Categoria atual = buscar(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe uma categoria com o nome '" + nome + "'");
        }
        atual.setNome(nome);
        return atual;
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscar(id);
        if (!categoria.getTarefas().isEmpty()) {
            throw new ConflitoException("A categoria " + id + " possui tarefas e não pode ser excluída");
        }
        repository.delete(categoria);
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O campo 'nome' é obrigatório");
        }
        String limpo = nome.strip();
        if (limpo.length() > Categoria.TAMANHO_NOME) {
            throw new DadosInvalidosException(
                    "O campo 'nome' deve ter no máximo " + Categoria.TAMANHO_NOME + " caracteres");
        }
        return limpo;
    }
}
