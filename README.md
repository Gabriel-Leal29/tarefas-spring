# Gerenciador de Tarefas: Spring Boot + JPA + MySQL + Docker

API REST de gerenciamento de tarefas e categorias feita com **Spring Boot**, persistindo os dados em **MySQL**
via **JPA/Hibernate**. O ambiente inteiro (banco + aplicação) sobe com **Docker Compose**.

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Spring MVC, Spring Data JPA)
- Hibernate 7 (implementação do JPA)
- MySQL 8.4
- Docker e Docker Compose
- Lombok (getters e setters das entidades)
- Maven (empacotamento WAR executável)
- H2 em memória, só nos testes

## Como executar

### Tudo no Docker (banco + aplicação)

Pré-requisito: Docker Desktop (ou Docker Engine com o plugin compose) rodando.

```bash
docker compose up -d
```

Na primeira vez o Docker baixa as imagens e compila o projeto dentro de um container, então demora alguns
minutos. Depois disso a API fica em `http://localhost:8080`.

Comandos úteis:

| Comando | O que faz |
|---|---|
| `docker compose ps` | mostra os containers e se o banco já está `healthy` |
| `docker compose logs -f app` | acompanha o log da aplicação |
| `docker compose up -d --build` | recompila a imagem da aplicação depois de alterar o código |
| `docker compose down` | para e remove os containers e a rede (**os dados continuam no volume**) |
| `docker compose down -v` | idem, e apaga também o volume (zera o banco) |
| `docker exec -it tarefas-db mysql -utarefas -ptarefas tarefas` | abre o cliente do MySQL no banco da aplicação |

### Aplicação pelo IntelliJ, banco no Docker

Útil para depurar. Sobe só o MySQL:

```bash
docker compose up -d db
```

e rode `TarefasSpringApplication` pelo IntelliJ. Sem as variáveis de ambiente do compose, o `application.properties`
usa os valores padrão (`localhost:3306`, banco/usuário/senha `tarefas`), que batem com o MySQL do compose.

Se aparecer um erro dizendo que não há `ServletWebServerFactory`, a configuração de execução não incluiu o
Tomcat (ele está com escopo `provided` no `pom.xml`). Em *Edit Configurations > Modify options*, marque
*Add dependencies with "provided" scope to classpath*. Outra opção: aba Maven > Plugins > spring-boot > `spring-boot:run`.

## Docker

### Arquivos

| Arquivo | Função |
|---|---|
| `Dockerfile` | receita da imagem da aplicação, em duas etapas (build com Maven, execução só com o JRE) |
| `.dockerignore` | o que não entra no contexto do build (target/, .git/, .idea/...) |
| `docker-compose.yml` | define os serviços `db` e `app`, o volume `dados-mysql` e a rede `rede-tarefas` |
| `.env.example` | exemplo das variáveis que podem ser trocadas (copie para `.env`) |

### Como os serviços se conectam

```
                 rede-tarefas (bridge)
   ┌──────────────────────────────────────────────┐
   │  app (tarefas-app)         db (tarefas-db)    │
   │  Spring Boot :8080  ─────▶ MySQL :3306        │
   │  DB_HOST=db                 │                 │
   └────────┬────────────────────┼─────────────────┘
            │ 8080:8080          │ volume dados-mysql → /var/lib/mysql
         host (navegador, Postman)
```

- Dentro da rede do compose, cada serviço é encontrado pelo **nome**: a aplicação se conecta em `db:3306`.
- O `depends_on` com `condition: service_healthy` faz a aplicação esperar o MySQL aceitar conexões
  (healthcheck com `mysqladmin ping`), e não só o container do banco ser criado.
- O volume nomeado `dados-mysql` guarda os arquivos do MySQL fora do container: `docker compose down`
  e um novo `up -d` mantêm as tarefas cadastradas.

### Variáveis de ambiente

O `docker-compose.yml` usa `${VARIAVEL:-padrão}`: sem `.env`, valem os padrões abaixo.

| Variável | Padrão | Uso |
|---|---|---|
| `DB_NAME` | `tarefas` | banco criado no MySQL e usado pela aplicação |
| `DB_USER` | `tarefas` | usuário criado no MySQL e usado pela aplicação |
| `DB_PASSWORD` | `tarefas` | senha desse usuário |
| `DB_ROOT_PASSWORD` | `root` | senha do root do MySQL |
| `DB_PORT_HOST` | `3306` | porta do MySQL no computador |
| `APP_PORT_HOST` | `8080` | porta da API no computador |

As credenciais do banco só são aplicadas quando o volume está vazio (primeira inicialização). Para trocá-las depois,
é preciso recriar o volume com `docker compose down -v`.

## JPA/Hibernate

- Configuração em `application.properties`: URL, usuário e senha vêm de `DB_HOST`, `DB_PORT`, `DB_NAME`,
  `DB_USER`, `DB_PASSWORD`; `ddl-auto=update` cria/ajusta as tabelas a partir das entidades;
  `show-sql=true` mostra o SQL gerado no log; `open-in-view=false` restringe a sessão do Hibernate às transações do service.
- `Tarefa` e `Categoria` são `@Entity`, com id `@GeneratedValue(strategy = IDENTITY)` (AUTO_INCREMENT no MySQL).
- Enums gravados como texto (`@Enumerated(EnumType.STRING)`).
- Relacionamento **1:N**: uma categoria tem várias tarefas.
  - `Tarefa.categoria`: `@ManyToOne` + `@JoinColumn(name = "categoria_id")`, lado dono (tem a chave estrangeira).
  - `Categoria.tarefas`: `@OneToMany(mappedBy = "categoria")`, lado inverso, carregado sob demanda (LAZY) e fora do JSON.
- Repositórios são interfaces `JpaRepository`; a listagem filtrada de tarefas é uma consulta JPQL (`@Query`),
  e as de categoria são derivadas do nome do método (`existsByNomeIgnoreCase`).

Tabelas geradas pelo Hibernate no MySQL:

```
categorias (id PK, nome UNIQUE)
tarefas    (id PK, titulo, descricao, status, prioridade, categoria_id FK → categorias.id)
```

## Estrutura

```
src/main/java/com/unifor/tarefas_spring/
├── TarefasSpringApplication.java   main(): sobe o Spring e o Tomcat embutido
├── ServletInitializer.java         entrada usada se o .war for publicado em um servidor externo
├── controller/
│   ├── TarefaController.java       endpoints de /tarefas
│   ├── CategoriaController.java    endpoints de /categorias
│   └── TratadorDeErros.java        exceções → {"status", "mensagem"} (@RestControllerAdvice)
├── service/
│   ├── TarefaService.java          regras de tarefa (título obrigatório, padrões, categoria existente)
│   └── CategoriaService.java       regras de categoria (nome obrigatório e único, exclusão só se vazia)
├── repository/
│   ├── TarefaRepository.java       JpaRepository + listagem com filtros (JPQL)
│   └── CategoriaRepository.java    JpaRepository + consultas derivadas
├── exception/
│   ├── TarefaNaoEncontradaException.java     vira 404
│   ├── CategoriaNaoEncontradaException.java  vira 404
│   ├── DadosInvalidosException.java          vira 400
│   └── ConflitoException.java                vira 409
└── model/
    ├── Tarefa.java                 @Entity: id, titulo, descricao, status, prioridade, categoria
    ├── Categoria.java              @Entity: id, nome, tarefas
    ├── Status.java                 PENDENTE, EM_ANDAMENTO, CONCLUIDA
    ├── Prioridade.java             BAIXA, MEDIA, ALTA
    └── ErroResposta.java           corpo JSON das respostas de erro
```

## Contrato da API

URL base: `http://localhost:8080`.

### Tarefas

| Método | Endpoint | Corpo | Sucesso | Erros possíveis |
|---|---|---|---|---|
| GET | `/tarefas` | — | **200** + lista | 400 (filtro inválido) |
| GET | `/tarefas/{id}` | — | **200** + tarefa | 400 (id não numérico), 404 |
| POST | `/tarefas` | tarefa (JSON) | **201** + tarefa + header `Location` | 400, 415 |
| PATCH | `/tarefas/{id}` | só os campos a alterar (JSON) | **200** + tarefa atualizada | 400, 404, 415 |
| DELETE | `/tarefas/{id}` | — | **204** sem corpo | 400, 404 |

Filtros opcionais da listagem: `GET /tarefas?status=PENDENTE&prioridade=ALTA`.

```json
{
  "id": 1,
  "titulo": "Estudar Docker",
  "descricao": "Montar o docker-compose com MySQL",
  "status": "EM_ANDAMENTO",
  "prioridade": "ALTA",
  "categoria": { "id": 1, "nome": "Faculdade" }
}
```

- `id`: gerado pelo banco. Se vier no corpo, é ignorado.
- `titulo`: obrigatório no POST, até 150 caracteres. No PATCH, se enviado, não pode ser vazio.
- `descricao`: opcional, até 1000 caracteres.
- `status`: `PENDENTE` | `EM_ANDAMENTO` | `CONCLUIDA`. Se omitido no POST, vale `PENDENTE`.
- `prioridade`: `BAIXA` | `MEDIA` | `ALTA`. Se omitida no POST, vale `MEDIA`.
- `categoria`: opcional. Envie só o id: `"categoria": { "id": 1 }`. Categoria inexistente → 400.
  No PATCH, enviar a categoria troca a tarefa de categoria.
- O PATCH altera somente os campos enviados; os demais continuam iguais.
- Campos desconhecidos no JSON são ignorados.

### Categorias

| Método | Endpoint | Corpo | Sucesso | Erros possíveis |
|---|---|---|---|---|
| GET | `/categorias` | — | **200** + lista | — |
| GET | `/categorias/{id}` | — | **200** + categoria | 400 (id não numérico), 404 |
| GET | `/categorias/{id}/tarefas` | — | **200** + tarefas da categoria | 400, 404 |
| POST | `/categorias` | `{"nome": "..."}` | **201** + categoria + header `Location` | 400, 409, 415 |
| PUT | `/categorias/{id}` | `{"nome": "..."}` | **200** + categoria atualizada | 400, 404, 409, 415 |
| DELETE | `/categorias/{id}` | — | **204** sem corpo | 400, 404, 409 |

- `nome`: obrigatório, até 60 caracteres, único (sem diferenciar maiúsculas de minúsculas). Repetido → 409.
- Uma categoria que ainda tem tarefas não pode ser excluída → 409.

### Códigos de status

| Código | Quando |
|---|---|
| 200 OK | GET, PATCH e PUT bem-sucedidos |
| 201 Created | POST bem-sucedido, com header `Location: /tarefas/{id}` ou `/categorias/{id}` |
| 204 No Content | DELETE bem-sucedido |
| 400 Bad Request | JSON malformado ou ausente, campo obrigatório ausente/vazio, texto longo demais, enum inválido, id não numérico, categoria inexistente |
| 404 Not Found | tarefa/categoria inexistente ou URL inexistente |
| 405 Method Not Allowed | método não aceito na URL (ex.: `DELETE /tarefas`), com header `Allow` listando os aceitos |
| 409 Conflict | nome de categoria repetido, exclusão de categoria com tarefas |
| 415 Unsupported Media Type | POST/PATCH/PUT sem `Content-Type: application/json` |

### Formato dos erros

```json
{ "status": 404, "mensagem": "Tarefa 99 não encontrada" }
```

## Testes

**Automatizados:** `./mvnw test` roda os testes de integração (`ApiIntegracaoTests`), que exercitam a API de ponta a
ponta (controller → service → JPA) com o H2 em memória no lugar do MySQL.

**Postman:** importe `tarefas.postman_collection.json`, suba o ambiente com `docker compose up -d` e use
**Run collection**. As pastas rodam em ordem (Categorias → Tarefas → Limpeza) e os ids criados ficam nas variáveis
da coleção.
