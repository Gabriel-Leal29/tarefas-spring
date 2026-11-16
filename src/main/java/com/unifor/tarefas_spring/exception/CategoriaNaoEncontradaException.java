package com.unifor.tarefas_spring.exception;

/**
 * Lançada quando o id não corresponde a nenhuma categoria. Vira 404 em {@code TratadorDeErros}.
 */
public class CategoriaNaoEncontradaException extends RuntimeException {

    public CategoriaNaoEncontradaException(Long id) {
        super("Categoria " + id + " não encontrada");
    }
}
