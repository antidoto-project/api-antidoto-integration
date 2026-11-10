# SOS Peçonha — MVP (Global Solution 2026/2)

> **Este pacote substitui a especificação anterior para fins de implementação.** A especificação completa
> (pasta `docs/`) continua valendo como **visão de futuro** para o pitch e o roadmap.
>
> Este arquivo é a **fonte única** das regras compartilhadas entre o app e o backend Java.
> - App mobile → `MVP-02-FRONT-MOBILE.md`
> - Backend Java → `MVP-01-BACKEND-JAVA.md`
> - Contrato das rotas (vale para os dois) → `api/openapi-mvp.yaml`

---

## 1. O produto em 1 minuto

**Problema:** os acidentes com escorpiões, serpentes, aranhas, lagartas e abelhas crescem no Brasil (mais de 225 mil
picadas de escorpião em 2025, com as mortes dobrando). O que mata é o **tempo**: a pessoa não sabe a gravidade, o que fazer,
nem **qual unidade de saúde tem o soro certo**.

**MVP:** um app em que o cidadão:
1. Aperta **SOS** (funciona **sem login**), responde 3 perguntas e vê a **urgência** e o que fazer.
2. Vê as **unidades mais próximas que têm o soro compatível**, com os botões "Ligar" e "Rota".
3. (Logado) **Registra a ocorrência**. O backend gera, de forma assíncrona (RabbitMQ + Spring AI), um **resumo de pré-atendimento**
   para mostrar na recepção da unidade.
4. (Logado) **Registra avistamentos** de animais peçonhentos (CRUD) para ajudar a prevenção no bairro.

**Princípio inegociável:** o app **nunca** diz que a pessoa não precisa de atendimento e **nunca** indica dose ou remédio.

---

## 2. O que entrou e o que saiu

| Entrou no MVP | Ficou para o roadmap (só no pitch) |
|---|---|
| Login/cadastro (JWT, 2 perfis: CIDADAO e GESTOR) | Refresh token, exclusão de conta, contatos de emergência |
| Triagem SOS **local** no app + mesma regra no back | Identificação do animal por foto dentro do app* |
| Unidades próximas com soro compatível (Java) | Estoque de soro com quantidades, idempotência, rate limit |
| Registro de ocorrência + resumo por IA (assíncrono) | Aviso em tempo real para a unidade e painel do gestor |
| CRUD de avistamentos | Mapa de risco, detecção de focos, alertas preventivos |
| Primeiros socorros (conteúdo local no app) | Modo offline completo, SMS, IoT (Sentinela UV) |
| CRUD de unidades (perfil GESTOR, via Swagger) | Integração do app com o .NET |

\* A disciplina de IA pode entregar a identificação por foto (Gemini) e o RAG em um **repositório próprio**, sem integração com o app.

**O app não consome o .NET.** O colega do .NET entrega a API da disciplina dele separada (sugestão: o mesmo domínio de unidades e soros).
O edital de .NET menciona o app, mas os critérios de nota dele não pontuam a integração com o Mobile.

---

## 3. Divisão e cronograma

| Quem | Faz |
|---|---|
| Você | App mobile + cloud/DevOps (deploy do **Java**) |
| Amigo (Java) | Backend Java (`MVP-01`) |
| Colega .NET | API .NET da disciplina (separada) |
| Colega IA / Banco / QA | Conforme os editais (IA separada; scripts Oracle/Mongo; backlog e testes no Azure DevOps) |

| Data | Marco |
|---|---|
| 07–09/11 | Java: auth + unidades + avistamentos no ar. App: telas com o mock do contrato |
| 10–12/11 | Java: ocorrências + urgência + RabbitMQ + Spring AI. App: SOS completo |
| **13/11** | **Prazo do cadastro do grupo** |
| 13–15/11 | Integração do app com o Java na nuvem; publicação no Firebase |
| 16–18/11 | Testes, vídeos (pitch Java, Mobile, DevOps), READMEs |
| **19/11** | **Entrega até as 23h55** (entregar antes!) |

---

## 4. Regras compartilhadas (o app e o Java implementam IGUAL)

### 4.1 Enums (os valores trafegam exatamente assim no JSON)

| Enum | Valores |
|---|---|
| `TipoAnimal` | `ESCORPIAO`, `SERPENTE`, `ARANHA`, `LAGARTA`, `ABELHA`, `NAO_SEI` |
| `FaixaEtaria` | `CRIANCA` (0–9 anos), `ADULTO` (10–59), `IDOSO` (60+) |
| `Urgencia` | `EMERGENCIA`, `ALTA`, `MODERADA` (**não existe** "baixa" nem "sem urgência") |
| `TipoSoro` | `ANTIESCORPIONICO`, `ANTIARACNIDICO`, `ANTIBOTROPICO`, `ANTICROTALICO`, `ANTIELAPIDICO`, `ANTILAQUETICO`, `ANTILOXOSCELICO`, `ANTILONOMICO` |
| `TipoUnidade` | `HOSPITAL`, `UPA`, `UBS` |
| `LocalAvistamento` | `CASA`, `QUINTAL`, `ESCOLA`, `TRABALHO_RURAL`, `VIA_PUBLICA`, `OUTRO` |
| `StatusResumo` | `PENDENTE`, `PRONTO`, `MODELO_PADRAO` |
| `Perfil` | `CIDADAO`, `GESTOR` |

### 4.2 Sintomas (código → texto na tela → é sinal de alerta?)

| Código | Texto | Alerta |
|---|---|---|
| `DOR_LOCAL` | Dor no local | não |
| `INCHACO_LOCAL` | Inchaço no local | não |
| `VERMELHIDAO` | Vermelhidão ou mancha roxa | não |
| `DORMENCIA` | Formigamento ou dormência | não |
| `FALTA_DE_AR` | Dificuldade para respirar | **sim** |
| `VISAO_TURVA` | Visão turva ou pálpebras caídas | **sim** |
| `VOMITOS` | Vômitos repetidos | **sim** |
| `SUOR_INTENSO` | Suor intenso pelo corpo | **sim** |
| `SONOLENCIA_DESMAIO` | Sonolência, agitação forte ou desmaio | **sim** |
| `SANGRAMENTO` | Sangramento ou urina escura | **sim** |
| `INCHACO_ROSTO` | Inchaço no rosto, lábios ou garganta | **sim** |
| `MUITAS_PICADAS_ALERGIA` | Muitas picadas (10+) ou alergia conhecida | **sim** |

### 4.3 Regra de urgência (RN-U)

```
nivel = MODERADA ; motivos = []

RN-U1  se algum sintoma é alerta                          → nivel = max(nivel, EMERGENCIA) ; "Sinal de alerta"
RN-U2  se tipoAnimal = ESCORPIAO e faixa = CRIANCA        → nivel = max(nivel, EMERGENCIA) ; "Escorpião em criança"
RN-U3  se tipoAnimal ∈ {SERPENTE, ARANHA, LAGARTA, NAO_SEI}→ nivel = max(nivel, ALTA)      ; "Animal com risco de agravamento"
RN-U4  se faixa ∈ {CRIANCA, IDOSO}                        → nivel = max(nivel, ALTA)       ; "Criança ou idoso"
RN-U5  (por último) se passaram ≥ 3 h desde a picada e nivel ≠ EMERGENCIA
                                                          → sobe um nível                  ; "Mais de 3 horas desde a picada"
RN-U6  se motivos vazio                                   → motivos = ["Avaliação necessária"]

ordem: MODERADA < ALTA < EMERGENCIA
ligar192 = (nivel == EMERGENCIA)
```

**Casos de teste obrigatórios (os mesmos no Jest e no JUnit):**

| # | Animal | Faixa | Sintomas | Picada há | Esperado |
|---|---|---|---|---|---|
| 1 | ESCORPIAO | ADULTO | DOR_LOCAL | 10 min | MODERADA |
| 2 | ESCORPIAO | CRIANCA | — | 10 min | EMERGENCIA |
| 3 | ESCORPIAO | IDOSO | — | 10 min | ALTA |
| 4 | SERPENTE | ADULTO | — | 10 min | ALTA |
| 5 | ARANHA | ADULTO | FALTA_DE_AR | 10 min | EMERGENCIA |
| 6 | NAO_SEI | ADULTO | — | 10 min | ALTA |
| 7 | ESCORPIAO | ADULTO | — | 4 h | ALTA |
| 8 | SERPENTE | ADULTO | — | 4 h | EMERGENCIA |
| 9 | ABELHA | ADULTO | MUITAS_PICADAS_ALERGIA | 10 min | EMERGENCIA |
| 10 | ABELHA | ADULTO | DOR_LOCAL | 10 min | MODERADA |

### 4.4 Compatibilidade animal → soro (RN-S)

| TipoAnimal | A unidade é compatível se tiver **algum** destes soros |
|---|---|
| `ESCORPIAO` | ANTIESCORPIONICO, ANTIARACNIDICO |
| `SERPENTE` | ANTIBOTROPICO, ANTICROTALICO, ANTIELAPIDICO, ANTILAQUETICO |
| `ARANHA` | ANTIARACNIDICO, ANTILOXOSCELICO |
| `LAGARTA` | ANTILONOMICO |
| `NAO_SEI` | precisa ter ANTIBOTROPICO **e** (ANTIESCORPIONICO **ou** ANTIARACNIDICO) |
| `ABELHA` | não há soro: vale qualquer `HOSPITAL` ou `UPA` (atendimento de urgência) |

- **RN-S1:** só entram unidades `ativa = true`.
- **RN-S2:** ordenar por distância (Haversine) crescente; devolver no máximo `limite` (padrão 5, máximo 10).
- **RN-S3:** o campo `sorosCompativeis` traz só os soros da unidade que servem para aquele animal (vazio para ABELHA).
- **RN-S4:** a lista vazia é uma resposta válida (200 `[]`). O app mostra "Nenhuma unidade com soro encontrada. Ligue 192."

### 4.5 Outras regras
- **RN-G1:** o SOS (triagem, resultado, unidades próximas, primeiros socorros) funciona **sem login**. Registrar a ocorrência exige login.
- **RN-G2:** toda tela de resultado mostra o texto fixo: **"Procure atendimento médico imediatamente."**
- **RN-G3:** urgência `EMERGENCIA` ⇒ botão **"Ligar 192 (SAMU)"** no topo, antes de qualquer outra informação.
- **RN-G4:** a hora da picada não pode estar no futuro (tolerância de 5 min) nem passar de 72 h.
- **RN-G5:** um avistamento só pode ser visto, editado ou excluído pelo **dono**.
- **RN-G6:** o resumo por IA **nunca** inclui nome, e-mail ou telefone, e nunca fala de dose ou remédio. Se a IA falhar, vale um resumo montado por modelo de texto (`MODELO_PADRAO`).
- **RN-G7:** o texto que acompanha o resumo por IA é: "Resumo gerado por IA. Não substitui avaliação profissional."
- **RN-G8:** telefones úteis: SAMU **192**, Bombeiros **193**, Disque-Intoxicação **0800 722 6001**.
