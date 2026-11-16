package com.unifor.tarefas_spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA mapeada para a tabela {@code categorias}. Uma categoria agrupa várias tarefas (1:N).
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
public class Categoria {

    public static final int TAMANHO_NOME = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = TAMANHO_NOME)
    private String nome;

    /**
     * Lado inverso do relacionamento: quem tem a chave estrangeira (categoria_id) é a tabela tarefas,
     * por isso o "mappedBy" aponta para o atributo {@code categoria} de {@link Tarefa}.
     * É carregado sob demanda (LAZY, padrão do @OneToMany) e fica fora do JSON da categoria,
     * senão a categoria mostraria as tarefas, que mostrariam a categoria, e assim por diante.
     */
    @OneToMany(mappedBy = "categoria")
    @OrderBy("id")
    @JsonIgnore
    private List<Tarefa> tarefas = new ArrayList<>();
}
