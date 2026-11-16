package com.unifor.tarefas_spring.repository;

import com.unifor.tarefas_spring.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * As consultas abaixo são derivadas do nome do método: o Spring Data monta o JPQL sozinho.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
