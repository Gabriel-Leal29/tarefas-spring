package com.unifor.tarefas_spring.model;

/**
 * Corpo JSON devolvido em todas as respostas de erro (400, 404, 405, 415...).
 */
public record ErroResposta(int status, String mensagem) {
}
