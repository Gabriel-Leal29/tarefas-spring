package com.unifor.tarefas_spring.exception;

/**
 * Lançada quando o id não corresponde a nenhuma tarefa. Vira 404 em {@code TratadorDeErros}.
 */
public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException(Long id) {
        super("Tarefa " + id + " não encontrada");
    }
}
