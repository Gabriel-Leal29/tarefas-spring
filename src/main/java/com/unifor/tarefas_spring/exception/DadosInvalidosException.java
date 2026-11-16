package com.unifor.tarefas_spring.exception;

/**
 * Lançada quando os dados da tarefa não passam na validação. Vira 400 em {@code TratadorDeErros}.
 */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
