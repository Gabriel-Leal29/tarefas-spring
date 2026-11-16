package com.unifor.tarefas_spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidade JPA mapeada para a tabela {@code tarefas}. Também é usada como corpo das requisições
 * POST e PATCH: no PATCH, os campos que vierem nulos (não enviados) não são alterados.
 * Os getters e setters são gerados pelo Lombok.
 */
@Entity
@Table(name = "tarefas")
@Getter
@Setter
public class Tarefa {

    public static final int TAMANHO_TITULO = 150;
    public static final int TAMANHO_DESCRICAO = 1000;

    /** Chave primária gerada pelo banco (AUTO_INCREMENT no MySQL). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = TAMANHO_TITULO)
    private String titulo;

    @Column(length = TAMANHO_DESCRICAO)
    private String descricao;

    /** Gravado como texto (PENDENTE, EM_ANDAMENTO...), e não pela posição no enum. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade;
}
