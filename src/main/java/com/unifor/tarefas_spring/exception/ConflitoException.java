package com.unifor.tarefas_spring.exception;

/**
 * Lançada quando a operação conflita com dados já gravados (nome repetido, categoria com tarefas).
 * Vira 409 em {@code TratadorDeErros}.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
