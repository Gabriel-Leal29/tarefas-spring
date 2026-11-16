# Gerenciador de Tarefas: Spring Boot

API REST de gerenciamento de tarefas feita com **Spring Boot e Spring MVC** (parte 4.2 do trabalho).
Segue o mesmo contrato da versão Servlet: mesmos endpoints, códigos de status, formato de erro e valores padrão.
Os dados ficam apenas em memória e são perdidos quando a aplicação é reiniciada.

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Spring Web / Spring MVC)
- Jackson 3 (conversão JSON, já incluído pelo Spring Boot)
- Lombok (getters e setters de `Tarefa`)
- Maven (empacotamento WAR, como no exemplo do professor)
- Tomcat 11 embutido

## Estrutura

```
src/main/java/com/unifor/tarefas_spring/
├── TarefasSpringApplication.java   main(): sobe o Spring e o Tomcat embutido
├── ServletInitializer.java         entrada usada se o .war for publicado em um servidor externo
├── controller/
│   ├── TarefaController.java       endpoints (@RestController, @GetMapping, @PostMapping...)
│   └── TratadorDeErros.java        exceções → {"status", "mensagem"} (@RestControllerAdvice)
├── service/
│   └── TarefaService.java          regras: título obrigatório, padrões PENDENTE/MEDIA, 404
├── repository/
│   └── TarefaRepository.java       armazenamento em memória (ConcurrentHashMap + id sequencial)
├── exception/
│   ├── TarefaNaoEncontradaException.java   vira 404
│   └── DadosInvalidosException.java        vira 400
└── model/
    ├── Tarefa.java                 id, titulo, descricao, status, prioridade
    ├── Status.java                 PENDENTE, EM_ANDAMENTO, CONCLUIDA
    ├── Prioridade.java             BAIXA, MEDIA, ALTA
    └── ErroResposta.java           corpo JSON das respostas de erro
```

## Como executar

**Pelo IntelliJ:** abra `TarefasSpringApplication` e clique em Run no método `main`.
A API sobe em `http://localhost:8081/tarefas`.

A porta é 8081 (definida em `application.properties`) para não conflitar com o Payara da versão Servlet,
que usa a 8080. Assim as duas versões podem ficar no ar ao mesmo tempo.

Se aparecer um erro dizendo que não há `ServletWebServerFactory`, a configuração de execução não incluiu o
Tomcat (ele está com escopo `provided` no `pom.xml`). Em *Edit Configurations > Modify options*, marque
*Add dependencies with "provided" scope to classpath*. Outra opção: aba Maven > Plugins > spring-boot > `spring-boot:run`.

**Pelo .war:** aba Maven > Lifecycle > `package` gera `target/tarefas-spring.war`, que roda sozinho:

```
java -jar target/tarefas-spring.war
```

### E o Payara?

O Spring Boot 4 exige Jakarta EE 11 (Servlet 6.1), e o Payara 6 é Jakarta EE 10 (Servlet 6.0). Ao publicar o .war no
Payara 6, a aplicação até inicia, mas o deploy falha: o JSF do Payara é ativado e exige CDI, e o Jackson 2.18 interno
do Payara conflita com o Jackson 3 do Spring Boot. Por isso esta versão roda com o Tomcat embutido, que é o modo padrão
do Spring Boot. O .war gerado pode ser publicado em um servidor com Servlet 6.1, como o Tomcat 11.

## Contrato da API

URL base: `http://localhost:8081`.

| Método | Endpoint | Corpo | Sucesso | Erros possíveis |
|---|---|---|---|---|
| GET | `/tarefas` | — | **200** + lista | 400 (filtro inválido) |
| GET | `/tarefas/{id}` | — | **200** + tarefa | 400 (id não numérico), 404 |
| POST | `/tarefas` | tarefa (JSON) | **201** + tarefa + header `Location` | 400, 415 |
| PATCH | `/tarefas/{id}` | só os campos a alterar (JSON) | **200** + tarefa atualizada | 400, 404, 415 |
| DELETE | `/tarefas/{id}` | — | **204** sem corpo | 400, 404 |

Filtros opcionais da listagem: `GET /tarefas?status=PENDENTE&prioridade=ALTA`.

### Tarefa

```json
{
  "id": 1,
  "titulo": "Estudar Spring",
  "descricao": "Ler sobre @RestController",
  "status": "EM_ANDAMENTO",
  "prioridade": "ALTA"
}
```

- `id`: gerado pela aplicação (sequencial, começando em 1). Se vier no corpo, é ignorado.
- `titulo`: obrigatório no POST. No PATCH, se enviado, não pode ser vazio.
- `descricao`: opcional.
- `status`: `PENDENTE` | `EM_ANDAMENTO` | `CONCLUIDA`. Se omitido no POST, vale `PENDENTE`.
- `prioridade`: `BAIXA` | `MEDIA` | `ALTA`. Se omitida no POST, vale `MEDIA`.
- O PATCH altera somente os campos enviados; os demais continuam iguais.
- Campos desconhecidos no JSON são ignorados.

### Códigos de status

| Código | Quando |
|---|---|
| 200 OK | GET e PATCH bem-sucedidos |
| 201 Created | POST bem-sucedido, com header `Location: /tarefas/{id}` |
| 204 No Content | DELETE bem-sucedido |
| 400 Bad Request | JSON malformado ou ausente, título ausente/vazio, status ou prioridade inválidos, id não numérico |
| 404 Not Found | tarefa inexistente ou URL inexistente |
| 405 Method Not Allowed | método não aceito na URL (ex.: `DELETE /tarefas`), com header `Allow` listando os aceitos |
| 415 Unsupported Media Type | POST/PATCH sem `Content-Type: application/json` |

### Formato dos erros

```json
{ "status": 404, "mensagem": "Tarefa 99 não encontrada" }
```

As mensagens são as mesmas da versão Servlet.

## Testes com o Postman

Importe `tarefas.postman_collection.json` e use **Run collection**. É a mesma coleção da versão Servlet; só a variável
`baseUrl` foi trocada para `http://localhost:8081`.

## Diferenças de comportamento em relação à versão Servlet

Os casos do contrato acima respondem igual nas duas versões. Fora dele, há pequenas diferenças:

| Situação | Servlet | Spring |
|---|---|---|
| `Content-Type` das respostas | `application/json;charset=UTF-8` | `application/json` (o JSON é sempre UTF-8) |
| Ordem dos métodos no header `Allow` | fixa (`GET, POST`) | pode variar (`POST, GET`) |
| Método não suportado, ex.: `PUT /tarefas/1` | 405 com página HTML do Payara, sem `Allow` | 405 em JSON, com `Allow` |
| URL com barra no final (`/tarefas/`) | tratada como `/tarefas` | 404 (o Spring não ignora a barra final) |
| Texto sobrando depois do JSON (`{"titulo":"x"} xyz`) | aceito | 400 (o Jackson 3 é mais rígido) |

## Onde cada parte do contrato ficou

| O que precisa ser feito | Nesta versão |
|---|---|
| Direcionar a requisição para o método certo | `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping` em `TarefaController` |
| Ler o id da URL e os filtros | `@PathVariable Long id`, `@RequestParam Status status` (conversão automática) |
| JSON → objeto e objeto → JSON | `@RequestBody Tarefa` e o retorno do método (Jackson) |
| Status HTTP e header `Location` | `ResponseEntity.ok(...)`, `.created(uri)`, `.noContent()` |
| 405 + header `Allow` | automático (Spring MVC) |
| 415 | automático, pelo `consumes = MediaType.APPLICATION_JSON_VALUE` |
| Corpo `{"status", "mensagem"}` em todos os erros | `TratadorDeErros` (`@RestControllerAdvice`) |
| Criação dos objetos | o Spring cria `TarefaRepository`, `TarefaService` e `TarefaController` e injeta um no outro pelo construtor |
