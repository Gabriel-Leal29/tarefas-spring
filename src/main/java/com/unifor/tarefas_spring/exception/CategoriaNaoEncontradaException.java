package com.unifor.tarefas_spring.exception;

public class CategoriaNaoEncontradaException extends RuntimeException {

    public CategoriaNaoEncontradaException(Long id) {
        super("Categoria " + id + " não encontrada");
    }
}
