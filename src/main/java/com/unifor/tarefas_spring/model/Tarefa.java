package com.unifor.tarefas_spring.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Modelo da tarefa. Também é usado como corpo das requisições POST e PATCH:
 * no PATCH, os campos que vierem nulos (não enviados) não são alterados.
 * Os getters e setters são gerados pelo Lombok.
 */
@Getter
@Setter
public class Tarefa {

    private Long id;
    private String titulo;
    private String descricao;
    private Status status;
    private Prioridade prioridade;
}
