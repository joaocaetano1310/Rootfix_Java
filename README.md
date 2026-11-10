<div align="center">

# 📍 RootFix · API Java

**Zeladoria urbana por comunidade: o morador relata, a vizinhança apoia, a IA prioriza e o gestor resolve.**

Global Solution 2026/2 · FIAP · tema **CitizenTech** · grupo **Rooted**

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.2-6DB33F?logo=spring&logoColor=white)
![Gemini](https://img.shields.io/badge/Google-Gemini-4285F4?logo=googlegemini&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-Database-F80000?logo=oracle&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-4-FF6600?logo=rabbitmq&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)

</div>

---

## 👥 Integrantes

| Integrante | Responsabilidade no projeto |
|---|---|
| **João Victor Caetano** · RM 562074 | API Java e DevOps |
| **João Victor Bueno Castelini** | IA (Disruptive Architectures) e QA |
| **Felipe Furlanetto** | App Mobile (React Native) |
| **Raul Rezende** | Banco de dados (Oracle e MongoDB) |
| **Ryan Vetoriano** | API .NET (painel do gestor) |

---

## 📚 Sumário

1. [O problema](#-o-problema)
2. [A solução](#-a-solução)
3. [ODS da ONU](#-ods-da-onu)
4. [Funcionalidades](#-funcionalidades)
5. [Tecnologias](#-tecnologias)
6. [Arquitetura](#-arquitetura)
7. [Modelo de dados](#-modelo-de-dados)
8. [Como rodar](#-como-rodar)
9. [Usuários de teste](#-usuários-de-teste)
10. [Endpoints principais](#-endpoints-principais)
11. [Exemplos de requisição](#-exemplos-de-requisição)
12. [Requisito 5: opção A (RabbitMQ)](#-requisito-5-opção-a-rabbitmq)
13. [Funcionalidade com Spring AI](#-funcionalidade-com-spring-ai)
14. [Testes](#-testes)
15. [Deploy](#-deploy)
16. [Variáveis de ambiente](#-variáveis-de-ambiente)
17. [Estrutura do projeto](#-estrutura-do-projeto)
18. [Declaração de uso de IA](#-declaração-de-uso-de-ia)

---

## 🕳️ O problema

Buraco na rua, poste apagado, bueiro entupido, lixeira quebrada: quem mora no bairro convive com esses problemas todos os dias, mas eles chegam de forma solta e repetida a quem pode resolver. O mesmo buraco é relatado várias vezes, ninguém sabe se alguém já pediu o conserto, e a associação de moradores ou a secretaria não tem como saber o que é mais urgente.

## 💡 A solução

O **RootFix** organiza a zeladoria por **comunidade**:

1. **Gestores** (associações de bairro ou secretarias) criam comunidades para as áreas que atendem.
2. **Moradores** entram nas comunidades a até **3 km** de casa e publicam problemas com **foto e localização**.
3. Os vizinhos **apoiam** ("também sofri com isso") e acrescentam **observações**, em vez de publicar o mesmo problema de novo.
4. Uma **IA** olha a foto e estima a **gravidade**, e o gestor recebe uma **fila de triagem** com o que é mais grave e mais apoiado primeiro.
5. O gestor **adota** o problema, e todos os envolvidos são **notificados** a cada mudança de status.

Esta é a **API Java**, o backend principal: conta, login, comunidades, feed, apoios, observações, adoção, notificações, fila e IA. Ela é consumida pelo app React Native e pelo front web que vem junto no projeto. Depois da adoção, o acompanhamento do status é feito no painel do gestor da **API .NET**, que usa o mesmo banco e o mesmo token.

## 🌍 ODS da ONU

| ODS | Como o RootFix contribui |
|---|---|
| **11 · Cidades e comunidades sustentáveis** | Melhora a manutenção de ruas, calçadas, iluminação e drenagem, tornando o espaço público mais seguro e acessível (meta 11.7). |
| **9 · Indústria, inovação e infraestrutura** | Usa tecnologia (IA, mensageria e geolocalização) para priorizar a manutenção da infraestrutura urbana (meta 9.1). |
| **16 · Paz, justiça e instituições eficazes** | Dá ao cidadão um canal transparente de participação e acompanhamento das demandas do bairro (metas 16.6 e 16.7). |
| **3 · Saúde e bem-estar** | Ajuda a priorizar riscos que causam acidentes, como crateras, fiação exposta e alagamentos (meta 3.6). |

## ✨ Funcionalidades

| Morador | Gestor |
|---|---|
| Cadastro com endereço e coordenadas | Cadastro já criando a primeira comunidade |
| Encontrar comunidades a até 3 km | Criar, editar e excluir comunidades |
| Entrar e sair de comunidades | Ver o feed ordenado por gravidade e apoios |
| Publicar problema com foto | **Triagem da IA**: fila do que adotar primeiro |
| Apoiar (repetir remove o apoio) | Adotar problemas (status vira `PENDENTE`) |
| Comentar com observações | Pedir uma nova análise da IA |
| Editar ou excluir o próprio problema antes da adoção | Receber aviso de cada problema novo |
| Receber notificações com prioridade alta ou baixa | Acompanhar os supervisionados |

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.2 (Spring Framework 7) |
| Persistência | Spring Data JPA + Hibernate 7 · Oracle Database |
| Segurança | Spring Security 7 + JWT HS256 · perfis `GESTOR` e `MORADOR` |
| IA | Spring AI 2.0.2 · Google Gemini (texto + imagem, saída estruturada) |
| Mensageria | RabbitMQ 4 · Spring AMQP |
| Validação e erros | Bean Validation · `@RestControllerAdvice` |
| Documentação | springdoc-openapi 3.2.0 (Swagger UI) |
| Observabilidade | Spring Boot Actuator (`/actuator/health`) |
| Testes | JUnit · MockMvc · H2 em memória |
| Front web | HTML, CSS e JavaScript em `src/main/resources/static` |
| Contêiner | Docker (`dockerfiles/Dockerfile`) |

## 🏗️ Arquitetura

```mermaid
flowchart LR
    subgraph Clientes
        APP[📱 App React Native]
        WEB[🖥️ Front web]
    end

    subgraph API_Java[API Java · Spring Boot]
        SEC[Spring Security + JWT]
        CTRL[Controllers REST]
        SRV[Services · regras de negócio]
        PUB[Publicador de eventos]
        LIS[Listeners RabbitMQ]
        IA[Classificador · Spring AI]
    end

    NET[⚙️ API .NET · painel do gestor]
    DB[(🗄️ Oracle)]
    MQ[[🐇 RabbitMQ]]
    GEM[✦ Google Gemini]

    APP -->|HTTPS + JWT| SEC
    WEB -->|HTTPS + JWT| SEC
    SEC --> CTRL --> SRV --> DB
    SRV -->|após o commit| PUB --> MQ
    MQ --> LIS
    LIS --> IA -->|foto + descrição| GEM
    LIS -->|notificações| DB
    IA -->|gravidade| DB
    NET -->|mesmo JWT| DB
    NET -->|problema.status-alterado| MQ
    APP --> NET
```

O fluxo de uma publicação, da foto até a triagem:

```mermaid
sequenceDiagram
    actor M as Morador
    participant API as API Java
    participant DB as Oracle
    participant MQ as RabbitMQ
    participant L as Listener
    participant G as Gemini
    actor GS as Gestor

    M->>API: POST /comunidades/{id}/problemas (foto)
    API->>DB: grava o problema e a foto
    API-->>M: 201 Created
    API->>MQ: problema.publicado (após o commit)
    MQ->>L: entrega o evento
    L->>DB: notificação para o gestor (alta)
    L->>G: título + descrição + foto
    G-->>L: categoria, gravidade, justificativa
    L->>DB: grava a classificação
    GS->>API: GET /triagem
    API-->>GS: fila do mais grave ao menos grave
```

## 🗄️ Modelo de dados

São 10 tabelas, todas criadas por [`db/ddl.sql`](db/ddl.sql). A API não cria tabelas: ela só confere se o banco bate com as entidades (`ddl-auto=validate`).

```mermaid
erDiagram
    RF_USUARIO ||--o{ RF_COMUNIDADE : "gerencia"
    RF_USUARIO ||--o{ RF_PARTICIPACAO : "participa"
    RF_COMUNIDADE ||--o{ RF_PARTICIPACAO : "tem membros"
    RF_COMUNIDADE ||--o{ RF_PROBLEMA : "recebe"
    RF_USUARIO ||--o{ RF_PROBLEMA : "publica"
    RF_CATEGORIA ||--o{ RF_PROBLEMA : "classifica"
    RF_PROBLEMA ||--o| RF_FOTO_PROBLEMA : "tem"
    RF_PROBLEMA ||--o{ RF_APOIO : "recebe"
    RF_USUARIO ||--o{ RF_APOIO : "apoia"
    RF_PROBLEMA ||--o{ RF_OBSERVACAO : "recebe"
    RF_USUARIO ||--o{ RF_OBSERVACAO : "escreve"
    RF_PROBLEMA ||--o{ RF_HISTORICO_STATUS : "registra"
    RF_USUARIO ||--o{ RF_NOTIFICACAO : "recebe"
    RF_PROBLEMA ||--o{ RF_NOTIFICACAO : "gera"

    RF_USUARIO {
        number ID PK
        varchar NOME
        varchar EMAIL UK
        varchar PERFIL "GESTOR ou MORADOR"
        number LATITUDE
        number LONGITUDE
    }
    RF_COMUNIDADE {
        number ID PK
        varchar NOME
        number LATITUDE
        number LONGITUDE
        number ID_GESTOR FK
    }
    RF_PROBLEMA {
        number ID PK
        varchar TITULO
        number ADOTADO
        varchar STATUS
        varchar GRAVIDADE_IA
        varchar JUSTIFICATIVA_IA
    }
```

| Tabela | Para quê |
|---|---|
| `RF_USUARIO` | Gestores e moradores |
| `RF_COMUNIDADE` | Comunidades criadas pelos gestores |
| `RF_PARTICIPACAO` | Morador ↔ comunidade (único por par) |
| `RF_CATEGORIA` | Tipos de problema (8 categorias iniciais) |
| `RF_PROBLEMA` | Problemas publicados no feed |
| `RF_FOTO_PROBLEMA` | Foto do problema (BLOB, até 5 MB) |
| `RF_APOIO` | "Também sofri com isso" (único por usuário e problema) |
| `RF_OBSERVACAO` | Detalhes adicionados pelos membros |
| `RF_HISTORICO_STATUS` | Mudanças de status, gravadas pela API .NET |
| `RF_NOTIFICACAO` | Avisos com prioridade alta ou baixa |

## 🚀 Como rodar

### Pré-requisitos

- JDK 21 e Maven 3.9+
- Docker Desktop (para o RabbitMQ local)
- Um Oracle acessível (o da FIAP serve)
- Opcional: uma chave grátis do Gemini em https://aistudio.google.com/apikey

### Passo a passo

**1. Clone o repositório**

```bash
git clone https://github.com/joaocaetano1310/Rootfix_Java.git
cd Rootfix_Java
```

**2. Crie as tabelas.** Rode [`db/ddl.sql`](db/ddl.sql) como script no SQL Developer (F5). No fim, aparecem as 10 tabelas `RF_*`.

**3. Configure as variáveis.** Copie `.env.example` para `.env` e preencha pelo menos `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` (32 caracteres ou mais). Para ligar a IA, defina também `IA_PROVEDOR=google-genai` e `GEMINI_API_KEY`. O `.env` está no `.gitignore` e nunca vai para o repositório.

**4. Suba o RabbitMQ**

```bash
docker compose up -d
```

Painel em http://localhost:15672 (usuário `guest`, senha `guest`).

**5. Rode a API**

```bash
mvn clean spring-boot:run
```

**6. Acesse**

| O quê | Endereço |
|---|---|
| Front web | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Saúde da API | http://localhost:8080/actuator/health |

### Rodando com Docker

```bash
docker build -f dockerfiles/Dockerfile -t rootfix-api .
docker run --env-file .env -p 8080:8080 rootfix-api
```

## 🔑 Usuários de teste

Eles são criados automaticamente quando a API sobe. Para desligar, use `DADOS_INICIAIS=false`.

| Perfil | E-mail | Senha | O que já tem |
|---|---|---|---|
| 🏛️ Gestor | `gestor@rootfix.com` | `Rootfix@2026` | Comunidades "Bela Vista - Av. Paulista" e "Mooca - Rua da Mooca" |
| 🏠 Morador | `morador@rootfix.com` | `Rootfix@2026` | Endereço na Rua Frei Caneca e membro da Bela Vista |

> A Mooca fica a cerca de 5,7 km do endereço do morador: tentar entrar nela demonstra a regra dos 3 km.

No front web, a tela de entrada tem os botões **Entrar como morador** e **Entrar como gestor**. No Swagger, faça `POST /auth/login`, copie o `token` e clique em **Authorize**.

## 📡 Endpoints principais

**Autenticação**

| Método e rota | Quem | Para quê |
|---|---|---|
| `POST /auth/registro/morador` | público | Cria conta de morador |
| `POST /auth/registro/gestor` | público | Cria conta de gestor e a primeira comunidade |
| `POST /auth/login` | público | Devolve o JWT |
| `GET /usuarios/me` | logado | Dados da conta |
| `GET /usuarios/me/comunidades` | logado | Comunidades em que participo ou que gerencio |

**Comunidades**

| Método e rota | Quem | Para quê |
|---|---|---|
| `POST /comunidades` | GESTOR | Cria comunidade |
| `GET /comunidades/proximas?page&size` | MORADOR | Comunidades a até 3 km, paginadas |
| `GET /comunidades/{id}` | logado | Detalhe com o gestor |
| `PUT /comunidades/{id}` | gestor dono | Edita |
| `DELETE /comunidades/{id}` | gestor dono | Exclui (se não tiver problemas) |
| `POST /comunidades/{id}/membros` | MORADOR | Entra (valida os 3 km) |
| `DELETE /comunidades/{id}/membros` | MORADOR | Sai |

**Problemas, apoios e observações**

| Método e rota | Quem | Para quê |
|---|---|---|
| `POST /comunidades/{id}/problemas` | membro | Publica problema (multipart, foto opcional até 5 MB) |
| `GET /comunidades/{id}/problemas?ordem&page&size` | membro | Feed paginado: `recentes`, `apoiados` ou `gravidade` |
| `GET /comunidades/{id}/supervisionados` | membro | Problemas adotados, com status |
| `GET /problemas/{id}` | membro | Detalhe |
| `GET /problemas/{id}/foto` | membro | Imagem |
| `PUT /problemas/{id}` | autor | Edita antes da adoção |
| `DELETE /problemas/{id}` | autor | Exclui antes da adoção |
| `POST /problemas/{id}/apoios` | membro | Apoia (repetir remove) |
| `DELETE /problemas/{id}/apoios` | membro | Retira o apoio |
| `GET /problemas/{id}/observacoes` | membro | Lista observações |
| `POST /problemas/{id}/observacoes` | membro | Adiciona observação |
| `DELETE /observacoes/{id}` | autor | Remove a própria observação |

**Gestão, IA e notificações**

| Método e rota | Quem | Para quê |
|---|---|---|
| `POST /problemas/{id}/adotar` | gestor dono | Adota (status `PENDENTE`) |
| `GET /triagem?page&size` | GESTOR | Fila da IA: não adotados, do mais grave ao menos grave |
| `POST /problemas/{id}/reclassificar` | gestor dono | Pede nova análise da IA |
| `GET /notificacoes?page&size` | logado | Minhas notificações |
| `PATCH /notificacoes/{id}/lida` | logado | Marca como lida |
| `GET /categorias` | público | Categorias de problema |
| `GET /actuator/health` | público | Saúde da API, do banco e da fila |

## 🧪 Exemplos de requisição

**Login**

```http
POST /auth/login
Content-Type: application/json

{ "email": "morador@rootfix.com", "senha": "Rootfix@2026" }
```

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEmSegundos": 28800,
  "usuario": { "id": 2, "nome": "João Silva", "email": "morador@rootfix.com", "perfil": "MORADOR" }
}
```

**Publicar um problema** (multipart)

```bash
curl -X POST http://localhost:8080/comunidades/1/problemas \
  -H "Authorization: Bearer SEU_TOKEN" \
  -F "titulo=Cratera na Rua Frei Caneca" \
  -F "descricao=Buraco grande em frente ao número 980, carros desviam pela calçada" \
  -F "latitude=-23.5590" \
  -F "longitude=-46.6550" \
  -F "foto=@buraco.jpg"
```

**Problema depois da análise da IA**

```json
{
  "id": 2,
  "titulo": "Cratera na Rua Frei Caneca",
  "categoria": { "id": 1, "nome": "Buraco ou pavimento" },
  "gravidadeIa": "CRITICA",
  "justificativaIa": "Cratera de grande porte no pavimento, com risco imediato para veículos e pedestres.",
  "adotado": false,
  "totalApoios": 3,
  "temFoto": true
}
```

**Erro padronizado** (`@RestControllerAdvice`)

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Confira os campos informados.",
  "caminho": "/auth/registro/morador",
  "timestamp": "2026-11-10T01:20:00",
  "campos": [{ "campo": "email", "mensagem": "E-mail inválido." }]
}
```

| Status | Quando |
|---|---|
| `400` | Campo inválido ou JSON malformado |
| `401` | Sem token, token inválido ou login errado |
| `403` | Perfil errado ou sem permissão (não é membro, não é o autor, não é o gestor dono) |
| `404` | Recurso não encontrado |
| `409` | Conflito (e-mail já usado, problema já adotado) |
| `413` | Foto maior que 5 MB |
| `422` | Regra de negócio (fora dos 3 km, problema já adotado não pode ser editado) |

## 🐇 Requisito 5: opção A (RabbitMQ)

A API publica eventos numa exchange do tipo *topic* (`rootfix.eventos`) **somente depois que a transação é confirmada no banco** (`@TransactionalEventListener`). Listeners `@RabbitListener` consomem cada fila de forma **assíncrona**.

| Evento (routing key) | Fila | Quem publica | O que o consumidor faz |
|---|---|---|---|
| `problema.publicado` | `rootfix.problema.publicado` | API Java | Avisa o gestor (alta) e classifica com Spring AI |
| `observacao.adicionada` | `rootfix.observacao.adicionada` | API Java | Avisa quem publicou (alta) |
| `problema.adotado` | `rootfix.problema.adotado` | API Java | Avisa os envolvidos |
| `problema.status-alterado` | `rootfix.problema.status-alterado` | API .NET | Avisa os envolvidos |

**Regra de prioridade das notificações:** **alta** para quem publicou e quem fez observação, e **baixa** para quem apoiou e os demais membros da comunidade.

Mensagem esperada da API .NET:

```json
{ "problemaId": 12, "statusAnterior": "PENDENTE", "statusNovo": "EM_ANDAMENTO" }
```

Se o RabbitMQ estiver fora do ar, a API continua respondendo: o envio só fica registrado no log, e a requisição do usuário não falha.

## ✦ Funcionalidade com Spring AI

Quando um morador publica um problema, o listener de `problema.publicado` envia ao **Gemini** o título, a descrição e a **foto**. Com o `ChatClient` do Spring AI e **saída estruturada** (`.entity(ClassificacaoIA.class)`), a resposta vira um objeto Java:

| Campo | Conteúdo |
|---|---|
| `categoria` | Uma das categorias da tabela `RF_CATEGORIA` |
| `fotoMostraOProblema` | Se a foto mostra de fato o problema descrito |
| `gravidade` | `BAIXA`, `MEDIA`, `ALTA` ou `CRITICA` |
| `justificativa` | Uma frase curta explicando o que foi visto na foto |

| Gravidade | Critério |
|---|---|
| 🔴 **Crítica** | Risco imediato à vida ou à segurança (fiação exposta, cratera em via movimentada, alagamento) |
| 🟠 **Alta** | Risco de acidente ou de saúde |
| 🟡 **Média** | Atrapalha o dia a dia |
| ⚪ **Baixa** | Incômodo, sem risco |

**Regras de segurança da análise:**

- **Sem foto, não há gravidade.** A IA não é chamada, e o problema fica com o aviso de que não é possível avaliar sem ver o local.
- **Foto que não mostra o problema**, por estar escura, desfocada ou mostrar outra coisa, também fica sem gravidade, e a IA explica o motivo.
- A análise roda **fora da requisição** do morador. Se a IA falhar, demorar ou estiver desligada (`IA_PROVEDOR=none`), o problema só fica sem classificação, e o resto do sistema continua funcionando.

O resultado alimenta a tela **Triagem da IA** (`GET /triagem`): um resumo por gravidade e a fila do que adotar primeiro, com a justificativa. O gestor também pode pedir uma nova análise (`POST /problemas/{id}/reclassificar`).

## ✅ Testes

```bash
mvn test
```

São 14 testes, que rodam com H2 em memória. Não precisam de Oracle, de RabbitMQ nem de chave de IA.

| Regra | Teste |
|---|---|
| Só o autor edita ou exclui, e só antes da adoção | `RegrasDeNegocioTest.soOAutorEditaEExcluiESoAntesDaAdocao` |
| Só o gestor dono da comunidade adota | `RegrasDeNegocioTest.soOGestorDonoDaComunidadeAdota` |
| Morador a mais de 3 km não entra | `RegrasDeNegocioTest.moradorAMaisDe3KmNaoEntraNaComunidade` |
| Um apoio por usuário por problema | `RegrasDeNegocioTest.umApoioPorUsuarioPorProblema` |
| Quem não é membro não publica, não apoia e não observa | `RegrasDeNegocioTest.quemNaoEMembroNaoPublicaNaoApoiaNaoObserva` |
| Sem token: 401 · perfil errado: 403 | `SegurancaTest` |
| Validação dos campos e login errado | `SegurancaTest` |
| Cálculo da distância | `DistanciaTest` |

## ☁️ Deploy

| Ambiente | Onde | Banco | Fila |
|---|---|---|---|
| Desenvolvimento | Máquina local | Oracle da FIAP | RabbitMQ em Docker |
| Produção para o app | Render (Docker, a partir do GitHub) | Oracle da FIAP | CloudAMQP |
| DevOps | Azure, via pipeline do Azure DevOps | Oracle em container | RabbitMQ em container |

**Link do deploy:** _a preencher depois de criar o serviço no Render._

> O plano gratuito do Render dorme após 15 minutos sem uso e leva cerca de 1 minuto para acordar.

Para configurar no Render: *New → Web Service* → linguagem **Docker** → Dockerfile em `./dockerfiles/Dockerfile` → health check em `/actuator/health` → as variáveis abaixo em *Environment*.

## ⚙️ Variáveis de ambiente

| Variável | Obrigatória | Exemplo / padrão |
|---|---|---|
| `DB_URL` | sim | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USERNAME` · `DB_PASSWORD` | sim | Usuário do Oracle |
| `JWT_SECRET` | sim | 32 caracteres ou mais; o mesmo valor na API .NET |
| `JWT_ISSUER` | não | `rootfix-api` |
| `JWT_EXPIRATION_MINUTES` | não | `480` |
| `RABBITMQ_URL` | não | `amqp://guest:guest@localhost:5672` |
| `RABBIT_HEALTH_ENABLED` | não | `false` |
| `IA_PROVEDOR` | não | `none` ou `google-genai` |
| `GEMINI_API_KEY` | com a IA ligada | Chave do Google AI Studio |
| `IA_MODELO` | não | `gemini-3.5-flash` |
| `DADOS_INICIAIS` | não | `true` |
| `PORT` | não | `8080` |

**Integração com a API .NET:** o token é assinado com **HS256**, emissor `JWT_ISSUER`, `sub` igual ao id do usuário e a claim `perfil` igual a `GESTOR` ou `MORADOR`.

## 📁 Estrutura do projeto

```
Rootfix_Java/
├── db/ddl.sql                    # script Oracle das 10 tabelas
├── dockerfiles/Dockerfile        # imagem da API
├── docker-compose.yml            # RabbitMQ local
├── .env.example                  # modelo das variáveis
└── src/
    ├── main/java/br/com/fiap/rootfix/
    │   ├── ai/                   # classificação com Spring AI
    │   ├── config/               # Swagger e contas de teste
    │   ├── controller/           # rotas REST
    │   ├── dto/                  # entrada e saída da API
    │   ├── entity/               # entidades JPA
    │   ├── exception/            # exceções e @RestControllerAdvice
    │   ├── messaging/            # RabbitMQ: filas, publicador e listeners
    │   ├── repository/           # Spring Data JPA
    │   ├── security/             # JWT, perfis e respostas 401/403
    │   └── service/              # regras de negócio
    ├── main/resources/
    │   ├── application.yml       # configuração por variáveis de ambiente
    │   └── static/               # front web (HTML, CSS, JS)
    └── test/                     # testes JUnit com MockMvc e H2
```
---

<div align="center">

**RootFix** · Global Solution 2026/2 · FIAP · grupo Rooted

</div>
