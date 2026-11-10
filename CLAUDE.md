# Antídoto API — Contrato de Desenvolvimento

API Java (Spring Boot) que o app **Antídoto** consome. **Não é a entrega da disciplina de Java**: existe para o app funcionar de verdade. Todo código gerado (por humano ou IA) segue estas regras, que descrevem o **estado real do código**. O que ainda não existe aparece com o passo em que entra — **(Passo N)**.

- **Fonte da verdade:** `docs/api/openapi-mvp.yaml` (rotas, campos, enums, erros). É **o mesmo arquivo do app**. Se o MVP-01 e o app discordarem, **vale o app**, e o Java se ajusta.
- **Regras de negócio:** `docs/MVP-00-LEIA-ME.md` (RN-U, RN-S, RN-G) e `docs/MVP-01-BACKEND-JAVA.md`.
- **Padrão:** o mesmo do Java do Challenge (java-afetto): Gradle, pacotes por camada, DTOs `record`, mapper `@Component`, injeção por construtor, getters e setters escritos (sem Lombok).
- **Idioma:** código, mensagens e comentários em português. Nomes reservados do framework ficam como são.

---

## 1. Stack

| Item       | Escolha                                                                                         |
| ---------- | ----------------------------------------------------------------------------------------------- |
| Java       | 21                                                                                              |
| Framework  | Spring Boot 4.0 (`webmvc`, `validation`, `data-jpa`, `flyway`, `actuator`)                      |
| Banco      | H2 em memória (modo PostgreSQL) no computador e nos testes; PostgreSQL no Render (`DB_URL`)     |
| Migração   | Flyway, `db/migration/V*__*.sql`, em SQL do PostgreSQL. `ddl-auto=validate`: nunca `update`     |
| Docs       | springdoc-openapi 3 → `/swagger-ui.html`                                                        |
| JSON       | Jackson 3 (`tools.jackson`), padrão do Boot 4. Datas `Instant` em ISO-8601 UTC                  |
| Segurança  | Spring Security + JWT (JJWT), BCrypt **(Passo 2)**                                              |
| Testes     | JUnit 5, AssertJ, MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc` ou `standaloneSetup`)   |
| Build      | Gradle wrapper (`./gradlew test`, `./gradlew bootJar`)                                          |

Biblioteca nova: só combinando antes.

---

## 2. Estrutura

```
br.com.fiap.antidoto
├── AntidotoApiApplication.java
├── config/       RelogioConfig (bean Clock), OpenApiConfig
├── controller/   (Passo 2+) AuthController, UsuarioController, UnidadeController, AvistamentoController, OcorrenciaController
├── dto/          PaginaResponse; erro/ (ErroResponse, CampoErroResponse); <entidade>/ XRequest, XResponse
├── exception/    TratadorGlobalDeErros, RecursoNaoEncontradoException, RegraDeNegocioException, ConflitoException
├── mapper/       (Passo 2+) XMapper: entidade → DTO
├── model/        Usuario, UnidadeSaude, Ocorrencia, Avistamento + enums (Urgencia tem maior()/proxima(); Sintoma tem texto e alerta)
├── repository/   interfaces JpaRepository
├── security/     (Passo 2) JwtService, JwtFiltro, SecurityConfig
└── service/      regras de negócio
```

---

## 3. Camadas

- **Controller:** recebe, valida com `@Valid`, chama o service e devolve o DTO com o status do contrato (201 no POST, 204 no DELETE). **Nenhuma regra de negócio.**
- **Service:** toda regra. `@Transactional` nas escritas e `@Transactional(readOnly = true)` nas leituras; converte para DTO **dentro** da transação (`open-in-view=false`).
- **Entidade nunca sai do service.** A resposta é sempre um `record` com **exatamente** os nomes do YAML.
- **Mapper:** `@Component` com a conversão entidade → DTO (como no Afetto).
- **Repository:** Spring Data JPA; consulta derivada do nome do método antes de `@Query`.
- **Injeção sempre por construtor.**
- **Tempo:** `Instant.now(relogio)` com o `Clock` injetado, nunca `Instant.now()` solto (os testes fixam o relógio).

---

## 4. Erros (MVP-01 §7)

Todo erro sai no formato do contrato, montado pelo `TratadorGlobalDeErros`:

```json
{ "status": 400, "erro": "Dados inválidos", "mensagem": "Um ou mais campos são inválidos.",
  "campos": [{ "campo": "email", "mensagem": "E-mail inválido" }],
  "caminho": "/api/auth/cadastro", "timestamp": "2026-11-10T22:15:00Z" }
```

| Situação                                                              | Como                                                 |
| --------------------------------------------------------------------- | ---------------------------------------------------- |
| `@Valid` no corpo, parâmetro da URL inválido, enum desconhecido       | 400 com `campos` (nome do campo no JSON)             |
| Regra que o Bean Validation não cobre (RN-G4, `dataHora` no futuro)   | `throw new RegraDeNegocioException("campo", "...")`  |
| Não existe **ou é de outro usuário**                                  | `RecursoNaoEncontradoException` → 404 (nunca 403)   |
| E-mail já cadastrado                                                  | `ConflitoException("E-mail já cadastrado.")` → 409   |
| Qualquer outra                                                        | 500 genérico; o detalhe só no log                    |

- `campos` só aparece quando há campo inválido.
- O nome em `campos` é o do JSON do contrato: o app usa para pôr a mensagem embaixo do campo certo.
- Mensagens em português, prontas para a tela.

---

## 5. Banco

- O schema é do Flyway (`V1__criar_tabelas.sql`). Mudou uma entidade? **Nova migração** `V2__...`; nunca editar uma migração que já rodou no Render.
- SQL do PostgreSQL que também roda no H2 em modo PostgreSQL: `BIGINT ... GENERATED BY DEFAULT AS IDENTITY`, `VARCHAR`, `BOOLEAN`, `DOUBLE PRECISION` (coordenadas), `TIMESTAMP(6) WITH TIME ZONE` (datas `Instant`).
- Enums gravados como texto (`@Enumerated(EnumType.STRING)`), com `CHECK` no banco.
- `Ocorrencia.motivos` é texto separado por `;`; na resposta vira lista.

---

## 6. Paginação

Parâmetros `pagina` (padrão 0) e `tamanho` (padrão 20, máximo 50). Resposta **sempre** `PaginaResponse<T>` (`conteudo`, `pagina`, `tamanho`, `totalElementos`, `totalPaginas`), nunca o `Page` do Spring.

---

## 7. O que nunca fazer

- Responder diferente do `openapi-mvp.yaml` (rota, campo, enum, status).
- Devolver entidade JPA ou `Page` do Spring.
- `ddl-auto=update/create`; editar migração que já rodou.
- Regra de negócio no controller; `@Autowired` em campo; `System.out.println`; `catch` vazio.
- 403 para recurso de outro usuário (é 404).
- Senha, token ou segredo no código ou no log; commitar o `.env`.
- Mandar nome, e-mail, telefone ou coordenadas para a IA; falar de dose ou remédio no resumo **(Passo 5/7)**.
- Deixar a ocorrência falhar por causa do resumo: sempre há o modelo padrão **(Passo 5)**.

---

## 8. Checklist antes de todo commit

```
[ ] ./gradlew test passando
[ ] Resposta igual ao openapi-mvp.yaml (nomes, status, formato de erro)
[ ] Regra no service, DTO record na resposta, injeção por construtor
[ ] Nenhum segredo no código; .env fora do Git
[ ] Migração nova em vez de editar a antiga
```

---

_Antídoto — Global Solution 2026/2 · FIAP_
