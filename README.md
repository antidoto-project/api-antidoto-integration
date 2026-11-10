# Antídoto — API Java

API REST que o app **Antídoto** consome (Global Solution 2026/2 · FIAP · CitizenTech).

> **Esta API é só para o app.** Ela existe para o app funcionar de verdade e para testar a
> integração. **Não é a entrega da disciplina de Java**: essa é feita à parte, sobre o mesmo
> contrato. Por isso as duas são trocáveis no app, só mudando o `EXPO_PUBLIC_API_URL`.

- **Contrato:** [`docs/api/openapi-mvp.yaml`](docs/api/openapi-mvp.yaml), o mesmo do app. Responder diferente dele quebra o app.
- **Regras:** [`docs/MVP-00-LEIA-ME.md`](docs/MVP-00-LEIA-ME.md) (urgência, soros, regras gerais) e [`docs/MVP-01-BACKEND-JAVA.md`](docs/MVP-01-BACKEND-JAVA.md).
- **Os dados de exemplo são fictícios.**

---

## 🚧 Andamento

| Passo | O que entra                                                                                | Situação |
| ----- | ------------------------------------------------------------------------------------------ | -------- |
| 1     | Base: Spring Boot, banco com Flyway, entidades, formato de erro, paginação, Swagger, Docker | ✅       |
| 2     | Login e cadastro (JWT), rotas protegidas, `/api/usuarios/me`, usuários de teste            | ⏳       |
| 3     | Unidades próximas com soro compatível (RN-S) e 12 unidades fictícias                       | ⏳       |
| 4     | CRUD de avistamentos (só o dono)                                                           | ⏳       |
| 5     | Ocorrências: urgência (RN-U) e resumo de pré-atendimento em segundo plano                  | ⏳       |
| 6     | Deploy no Render (Postgres) e o app apontando para a API                                   | ⏳       |
| 7     | (Opcional) Resumo com IA de verdade (Spring AI + Groq)                                     | ⏳       |

---

## 🛠️ Tecnologias

Java 21 · Spring Boot 4 (Web MVC, Validation, Data JPA, Actuator) · Flyway · H2 (computador e testes) ·
PostgreSQL (Render) · springdoc-openapi (Swagger) · JUnit 5 + MockMvc · Gradle (wrapper) · Docker.

---

## ▶️ Como rodar

Pré-requisito: **JDK 21**. O Gradle vem pelo wrapper; não precisa instalar.

```bash
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Saúde: http://localhost:8080/actuator/health

Sem nenhuma variável, a API usa o **H2 em memória** (os dados somem ao parar). O Flyway cria as
tabelas na subida.

**App no celular apontando para esta API:** no `.env` do app,
`EXPO_PUBLIC_API_URL=http://<ip do computador>:8080` (o mesmo Wi-Fi), e `npx expo start --clear`.

### Testes

```bash
./gradlew test
```

### Docker

```bash
docker build -t antidoto-api .
docker run -p 8080:8080 antidoto-api
```

---

## ⚙️ Variáveis de ambiente

Modelo em [`.env.example`](.env.example). Nenhuma é obrigatória no computador.

| Variável                           | Para quê                                                  |
| ---------------------------------- | --------------------------------------------------------- |
| `PORT`                             | Porta (o Render define sozinho). Padrão: 8080             |
| `DB_URL`, `DB_USUARIO`, `DB_SENHA` | Postgres do Render. Sem `DB_URL`, H2 em memória            |

---

## 📂 Estrutura

```
src/main/java/br/com/fiap/antidoto/
├── config/       RelogioConfig (Clock das regras de tempo), OpenApiConfig (Swagger)
├── controller/   recebe, valida (@Valid) e chama o service
├── dto/          records de request e response, com os nomes do contrato; PaginaResponse; erro/
├── exception/    TratadorGlobalDeErros (formato de erro do contrato) e as exceções da API
├── mapper/       entidade → DTO
├── model/        entidades JPA e enums
├── repository/   Spring Data JPA
├── security/     JWT e Spring Security (Passo 2)
└── service/      regras de negócio
src/main/resources/
├── application.properties
└── db/migration/ V1__criar_tabelas.sql (Flyway, SQL do PostgreSQL)
docs/             pacote do MVP (MVP-00, MVP-01) e o contrato
```

As convenções estão no [`CLAUDE.md`](CLAUDE.md).

---

## Diferenças em relação ao MVP-01 (decididas em 10/11)

Como esta API não é a entrega de Java, ela segue o contrato à risca, mas simplifica o que o app
não enxerga:

| No MVP-01                              | Aqui                                                                           |
| -------------------------------------- | ------------------------------------------------------------------------------ |
| Maven, Spring Boot 3.5                 | Gradle e Spring Boot 4, como no Java do Challenge (java-afetto)                |
| Oracle em produção                     | PostgreSQL no Render (o Render não tem Oracle); o SQL do Flyway é de Postgres  |
| RabbitMQ para o resumo                 | Resumo em segundo plano dentro da API (`@Async`), o modo sem fila do MVP-01    |
| `br.com.sospeconha.api`, "SOS Peçonha" | `br.com.fiap.antidoto`, "Antídoto" (o nome do app)                             |
| CRUD de unidades do GESTOR             | Fora: o app não usa                                                            |

---

_Antídoto — Global Solution 2026/2 · FIAP_
