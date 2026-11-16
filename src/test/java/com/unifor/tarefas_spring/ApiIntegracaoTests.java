package com.unifor.tarefas_spring;

import com.jayway.jsonpath.JsonPath;
import com.unifor.tarefas_spring.repository.CategoriaRepository;
import com.unifor.tarefas_spring.repository.TarefaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api;MODE=MySQL")
@AutoConfigureMockMvc
class ApiIntegracaoTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    void limparBanco() {
        tarefaRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    void crudDeTarefaComValoresPadrao() throws Exception {
        long id = idDe(criar("/tarefas", """
                {"id": 999, "titulo": "Estudar JPA"}""")
                .andExpect(jsonPath("$.id").value(not(999)))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.categoria").value(nullValue()))
                .andReturn().getResponse().getContentAsString());

        mvc.perform(patch("/tarefas/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"status": "CONCLUIDA"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estudar JPA"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        mvc.perform(get("/tarefas").param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/tarefas/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/tarefas/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Tarefa " + id + " não encontrada"));
    }

    @Test
    void tituloMaiorQueAColunaDevolve400() throws Exception {
        mvc.perform(post("/tarefas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"" + "x".repeat(151) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crudDeCategoria() throws Exception {
        long id = idDe(criar("/categorias", """
                {"nome": "  Faculdade  "}""")
                .andExpect(header().string("Location", matchesRegex("/categorias/\\d+")))
                .andExpect(jsonPath("$.nome").value("Faculdade"))
                .andReturn().getResponse().getContentAsString());

        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "FACULDADE"}"""))
                .andExpect(status().isConflict());

        mvc.perform(put("/categorias/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Estudos"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Estudos"));

        mvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tarefas").doesNotExist());

        mvc.perform(delete("/categorias/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/categorias/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Categoria " + id + " não encontrada"));
    }

    @Test
    void tarefaComCategoria() throws Exception {
        long estudos = idDe(criar("/categorias", "{\"nome\": \"Estudos\"}").andReturn().getResponse().getContentAsString());
        long casa = idDe(criar("/categorias", "{\"nome\": \"Casa\"}").andReturn().getResponse().getContentAsString());

        long tarefa = idDe(criar("/tarefas", "{\"titulo\": \"Ler sobre Docker\", \"categoria\": {\"id\": " + estudos + "}}")
                .andExpect(jsonPath("$.categoria.id").value(estudos))
                .andExpect(jsonPath("$.categoria.nome").value("Estudos"))
                .andReturn().getResponse().getContentAsString());
        criar("/tarefas", "{\"titulo\": \"Sem categoria\"}");

        mvc.perform(get("/tarefas")).andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/categorias/" + estudos + "/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Ler sobre Docker"));

        mvc.perform(delete("/categorias/" + estudos)).andExpect(status().isConflict());

        mvc.perform(post("/tarefas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "x", "categoria": {"id": 9999}}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A categoria 9999 não existe"));
        mvc.perform(post("/tarefas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "x", "categoria": {}}"""))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/tarefas/" + tarefa).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\": {\"id\": " + casa + "}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria.nome").value("Casa"))
                .andExpect(jsonPath("$.titulo").value("Ler sobre Docker"));
        mvc.perform(get("/categorias/" + estudos + "/tarefas")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(delete("/categorias/" + estudos)).andExpect(status().isNoContent());

        mvc.perform(get("/categorias/9999/tarefas")).andExpect(status().isNotFound());
    }

    private ResultActions criar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    private static long idDe(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
