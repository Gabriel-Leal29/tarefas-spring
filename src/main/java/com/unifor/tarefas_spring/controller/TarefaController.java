package com.unifor.tarefas_spring.controller;

import com.unifor.tarefas_spring.model.Prioridade;
import com.unifor.tarefas_spring.model.Status;
import com.unifor.tarefas_spring.model.Tarefa;
import com.unifor.tarefas_spring.service.TarefaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    /** GET /tarefas, com filtros opcionais: ?status=PENDENTE&prioridade=ALTA */
    @GetMapping
    public ResponseEntity<List<Tarefa>> listar(@RequestParam(required = false) Status status,
                                               @RequestParam(required = false) Prioridade prioridade) {
        return ResponseEntity.ok(service.listar(status, prioridade));
    }

    /** GET /tarefas/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    /** POST /tarefas: 201 Created + header Location com o endereço da nova tarefa */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tarefa> criar(@RequestBody Tarefa tarefa) {
        Tarefa salva = service.criar(tarefa);
        // Só o caminho (ex.: /tarefas/1), sem http://host:porta, igual à versão Servlet
        String caminho = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(salva.getId())
                .getPath();
        return ResponseEntity.created(URI.create(caminho)).body(salva);
    }

    /** PATCH /tarefas/{id}: altera só os campos enviados e devolve a tarefa atualizada */
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tarefa> editar(@PathVariable Long id, @RequestBody Tarefa alteracoes) {
        return ResponseEntity.ok(service.editar(id, alteracoes));
    }

    /** DELETE /tarefas/{id}: 204 No Content, sem corpo */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
