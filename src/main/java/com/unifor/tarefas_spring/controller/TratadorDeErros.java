package com.unifor.tarefas_spring.controller;

import com.unifor.tarefas_spring.exception.DadosInvalidosException;
import com.unifor.tarefas_spring.exception.TarefaNaoEncontradaException;
import com.unifor.tarefas_spring.model.ErroResposta;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;

@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<ErroResposta> tarefaNaoEncontrada(TarefaNaoEncontradaException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(DadosInvalidosException e) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Qualquer outra exceção: 500, sem expor detalhes internos ao cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> erroInesperado(Exception e, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor");
    }

    /** Exceções do Spring MVC: mantém o status e os headers definidos pelo Spring e troca só o corpo. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String mensagem = switch (ex) {
            case HttpRequestMethodNotSupportedException e -> "Método " + e.getMethod() + " não permitido em " + uri;
            case HttpMediaTypeNotSupportedException e ->
                    "O corpo da requisição deve ser JSON (Content-Type: application/json)";
            case HttpMessageNotReadableException e -> mensagemJsonInvalido(e);
            case MethodArgumentTypeMismatchException e -> mensagemParametroInvalido(e);
            case NoResourceFoundException e -> "Recurso não encontrado: " + uri;
            default -> body instanceof ProblemDetail problema ? problema.getDetail() : ex.getMessage();
        };
        return ResponseEntity.status(statusCode)
                .headers(headers)
                .body(new ErroResposta(statusCode.value(), mensagem));
    }

    /** JSON malformado/ausente ou com valor inválido em algum campo. */
    private String mensagemJsonInvalido(HttpMessageNotReadableException e) {
        // Ex.: {"status": "FEITO"}, valor que não existe no enum
        if (e.getCause() instanceof InvalidFormatException f) {
            String campo = f.getPath().isEmpty() ? "" : f.getPath().get(0).getPropertyName();
            String mensagem = "Valor inválido para o campo '" + campo + "': " + f.getValue();
            if (f.getTargetType() != null && f.getTargetType().isEnum()) {
                mensagem += ". Valores aceitos: " + Arrays.toString(f.getTargetType().getEnumConstants());
            }
            return mensagem;
        }
        return "JSON inválido ou ausente no corpo da requisição";
    }

    /** Parâmetro da URL que não pôde ser convertido: /tarefas/abc ou ?status=FEITO */
    private String mensagemParametroInvalido(MethodArgumentTypeMismatchException e) {
        Class<?> tipo = e.getRequiredType();
        if (tipo != null && tipo.isEnum()) {
            return "Valor inválido para o filtro '" + e.getName() + "': " + e.getValue()
                    + ". Valores aceitos: " + Arrays.toString(tipo.getEnumConstants());
        }
        return "O " + e.getName() + " deve ser um número inteiro: " + e.getValue();
    }

    private ResponseEntity<ErroResposta> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResposta(status.value(), mensagem));
    }
}
