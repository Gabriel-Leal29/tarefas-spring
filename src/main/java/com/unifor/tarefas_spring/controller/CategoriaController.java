package com.unifor.tarefas_spring.controller;

import com.unifor.tarefas_spring.model.Categoria;
import com.unifor.tarefas_spring.model.Tarefa;
import com.unifor.tarefas_spring.service.CategoriaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    /** GET /categorias */
    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /** GET /categorias/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    /** GET /categorias/{id}/tarefas: tarefas que pertencem à categoria */
    @GetMapping("/{id}/tarefas")
    public ResponseEntity<List<Tarefa>> listarTarefas(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarTarefas(id));
    }

    /** POST /categorias: 201 Created + header Location */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Categoria> criar(@RequestBody Categoria categoria) {
        Categoria salva = service.criar(categoria);
        String caminho = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(salva.getId())
                .getPath();
        return ResponseEntity.created(URI.create(caminho)).body(salva);
    }

    /** PUT /categorias/{id}: a categoria só tem o nome, então a edição troca o recurso inteiro */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Categoria> editar(@PathVariable Long id, @RequestBody Categoria categoria) {
        return ResponseEntity.ok(service.editar(id, categoria));
    }

    /** DELETE /categorias/{id}: 204, ou 409 se a categoria ainda tiver tarefas */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
