# Estudo Completo da API Unicheck

Este arquivo resume a construção da API do projeto Unicheck, explicando a estrutura, as camadas, os principais fluxos, as entidades, os endpoints e os pontos de atenção encontrados no código.

## 1. Visão Geral

A API foi construída em Java com Spring Boot 3.3.5 e Java 21. O objetivo principal é gerenciar um sistema de presença acadêmica com usuários, alunos, professores, turmas, disciplinas, aulas, horários, presenças por QR Code e sincronização offline.

Principais tecnologias:

- Spring Boot Web: criação dos endpoints REST.
- Spring WebSocket/STOMP: comunicação em tempo real entre API e app.
- Spring Data JPA: persistência das entidades no banco.
- PostgreSQL: banco de dados relacional.
- Spring Security: autenticação e autorização.
- JWT com `jjwt`: geração e validação de tokens.
- BCrypt: criptografia de senhas.
- Lombok: redução de código repetitivo em models e DTOs.
- Spring Validation: validação de alguns DTOs.
- Springdoc OpenAPI/Swagger: documentação interativa.
- ZXing: geração de QR Code em PNG.
- Docker: empacotamento da aplicação.

## 2. Estrutura do Projeto

```text
src/main/java/com/unicheck/Unicheckapi
├── Controller
├── Exception
├── dto
├── model
├── repository
├── security
├── service
└── UnicheckapiApplication.java
```

Responsabilidade de cada pasta:

- `Controller`: recebe requisições HTTP, lê parâmetros/body e chama os services.
- `service`: contém regras de negócio, validações, permissões e montagem das respostas.
- `repository`: interfaces Spring Data JPA para acessar o banco.
- `model`: entidades JPA mapeadas para tabelas.
- `dto`: objetos de entrada e saída usados para transportar dados.
- `security`: configuração de segurança, JWT, CORS, Swagger e usuário inicial.
- `Exception`: tratamento global de erros.
- `resources/application.yaml`: configuração de servidor, banco, JPA e JWT.
- `config`: configuração do WebSocket/STOMP.
- `ws`: DTOs e publisher de eventos em tempo real.

## 3. Como a API Foi Construída

A API segue uma arquitetura em camadas:

```text
Cliente HTTP
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Banco PostgreSQL
```

Exemplo prático:

1. O cliente chama `POST /aulas/iniciar`.
2. `AulaController` lê `disciplinaId` e `titulo`.
3. `AulaService` valida se o usuário pode acessar a disciplina.
4. O service cria uma entidade `Aula` ativa.
5. `AulaRepository` salva no PostgreSQL.
6. A API retorna a aula criada.

Essa separação deixa os controllers mais simples e concentra regras de negócio nos services.

## 4. Configuração Principal

Arquivo: `src/main/resources/application.yaml`

Configurações importantes:

- Servidor escuta em `0.0.0.0`.
- Porta padrão: `8080`, podendo ser sobrescrita por `PORT`.
- Banco configurado por variáveis:
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `DDL_AUTO`
- JPA usa PostgreSQL e `ddl-auto` padrão `update`.
- JWT usa `app.jwt.secret`.
- Importa `.env` local se existir.

Arquivo `.env.example` mostra o modelo esperado de variáveis.

## 5. Entidades do Domínio

### Usuario

Tabela: `usuarios`

Campos principais:

- `id`: UUID gerado pelo servidor.
- `clientId`: UUID vindo do cliente, usado em sincronização offline.
- `nome`
- `email`: único.
- `senha`
- `role`: `GESTOR`, `PROFESSOR` ou `ALUNO`.
- `fotoUrl`
- `ativo`
- `sincronizado`
- `criado`
- `atualizado`

Detalhe importante: `Usuario` usa herança JPA com `SINGLE_TABLE`, então `Aluno` e `Professor` ficam na mesma tabela `usuarios`, diferenciados por `tipo_usuario`.

### Aluno

Herda de `Usuario`.

Campos extras:

- `matricula`
- `turma`: relação muitos-para-um com `Turma`.

### Professor

Herda de `Usuario`.

Não adiciona campos próprios no momento.

### Turma

Tabela: `turmas`

Campos:

- `id`
- `clientId`
- `periodo`
- `curso`
- `identificacao`

### Disciplina

Tabela: `disciplinas`

Campos:

- `id`
- `clientId`
- `nome`
- `codigo`
- `turma`
- `professor`
- `ativa`
- `criado`
- `atualizado`

Regra estrutural importante:

- Existe restrição única para impedir o mesmo professor de ter mais de uma disciplina ativa na mesma turma: `professor_id + turma_id`.

### Aula

Tabela: `aulas`

Campos:

- `id`
- `clientId`
- `disciplina`
- `titulo`
- `qrToken`
- `dataHora`
- `ativa`

Uma aula começa ativa e pode ser encerrada.

### Presenca

Tabela: `presencas`

Campos:

- `id`
- `clientId`
- `aluno`
- `aula`
- `disciplina`
- `dataHora`

Restrições importantes:

- `client_id` único.
- Par `aluno_id + aula_id` único, evitando presença duplicada do mesmo aluno na mesma aula.

### HorarioAula

Tabela: `horarios_aula`

Campos:

- `id`
- `disciplina`
- `diaSemana`: `MONDAY`, `TUESDAY`, etc.
- `horaInicio`
- `horaFim`

### Matricula

Tabela: `matriculas`

Campos:

- `id`
- `aluno`
- `disciplina`
- `dataMatricula`

## 6. Autenticação e Segurança

Arquivos principais:

- `SecurityConfig.java`
- `JwtAuthenticationFilter.java`

- `JwtService.java`
- `DataInitializer.java`

### Login

Endpoint:

```http
POST /auth/login
```

Body:

```json
{
  "email": "gestor@unicheck.com",
  "senha": "admin123"
}
```

Resposta:

```json
{
  "token": "jwt...",
  "refreshToken": "refresh-token..."
}
```

O token contém:

- `sub`: email do usuário.
- `role`: papel do usuário.
- expiração de 24 horas.

### Refresh Token

O login agora retorna dois tokens:

- `token`: access token JWT de 24 horas, usado no header `Authorization`.
- `refreshToken`: token de 2 dias, usado para renovar a sessão sem pedir senha novamente.

O refresh token é salvo no banco apenas como hash SHA-256. O valor original existe somente no cliente.

Endpoints:

```http
POST /auth/refresh
POST /auth/logout
```

`/auth/refresh` recebe o refresh token atual e devolve um novo par `{ token, refreshToken }`.
Esse processo é chamado de rotação: o refresh antigo é apagado/revogado e um novo é emitido.

`/auth/logout` recebe o refresh token e o revoga.

### Uso do Token

Enviar nas requisições protegidas:

```http
Authorization: Bearer <token>
```

O filtro JWT:

1. Lê o header `Authorization`.
2. Valida o token.
3. Extrai email e role.
4. Cria autenticação no Spring Security com `ROLE_<role>`.

### Usuário Inicial

`DataInitializer` cria automaticamente um gestor se não existir:

- email: `gestor@unicheck.com`
- senha: `admin123`
- role: `GESTOR`

Essa senha deve ser trocada em produção.

### Regras de Autorização

Perfis:

- `GESTOR`: administração geral.
- `PROFESSOR`: aulas, presença e dados das próprias disciplinas.
- `ALUNO`: consulta de dados relacionados à própria turma e QR Code.

Principais regras em `SecurityConfig`:

- `/auth/**`, `/ws-native`, `/ws-native/**`, `/swagger-ui/**`, `/v3/api-docs/**`: público.
- `GET /disciplinas`: apenas gestor.
- `GET /disciplinas/minhas`: aluno, professor ou gestor.
- `GET /disciplinas/turma/**`: aluno, professor ou gestor.
- `GET /disciplinas/professor/**`: professor ou gestor.
- `GET /alunos`: apenas gestor.
- `GET /alunos/turma/**`: aluno, professor ou gestor.
- `POST /professores/**`, `/alunos/**`, `/turmas/**`, `/disciplinas/**`: apenas gestor.
- `PUT /turmas/**`, `/disciplinas/**`: apenas gestor.
- `DELETE /turmas/**`, `/disciplinas/**`, `/alunos/**`, `/professores/**`: apenas gestor.
- `POST /aulas/**` e `PATCH /aulas/**`: professor ou gestor.
- `GET /aulas/disciplina/**`: aluno, professor ou gestor.
- `GET /presencas/dashboard`: apenas gestor.
- `GET /presencas/aluno/**`: aluno ou gestor.
- `GET /presencas/dashboard/professor/**`, `/presencas/disciplina/**`, `/presencas/aula/**`: professor ou gestor.
- `/presencas/registrar`: apenas professor.
- `/presencas/sincronizar`: professor ou gestor.
- `/sync/offline`: professor ou gestor.
- `GET /offline/bootstrap`: aluno, professor ou gestor.
- `/qrcode/aluno/**`: apenas aluno.
- Demais rotas: exigem autenticação.

## 7. Endpoints da API

### Auth

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/auth/login` | Faz login e retorna access token + refresh token | `LoginRequestDTO` |
| POST | `/auth/refresh` | Renova a sessão e rotaciona refresh token | `RefreshRequestDTO` |
| POST | `/auth/logout` | Revoga refresh token | `RefreshRequestDTO` |
| GET | `/auth/me` | Retorna dados do usuário logado | Header `Authorization` |

Observação: `/auth/me` está liberado pela configuração de segurança por estar em `/auth/**`, mas o controller exige header `Authorization`.

### Usuários

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/usuarios` | Cria usuário genérico | `UsuarioRequestDTO` |
| GET | `/usuarios` | Lista usuários | - |
| GET | `/usuarios/{id}` | Busca usuário por ID | - |
| PUT | `/usuarios/{id}` | Atualiza usuário | `UsuarioRequestDTO` |
| DELETE | `/usuarios/{id}` | Remove usuário | - |

### Alunos

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/alunos` | Cria aluno | `AlunoRequestDTO` |
| GET | `/alunos` | Lista todos os alunos | - |
| GET | `/alunos/turma/{turmaId}` | Lista alunos de uma turma permitida | - |
| GET | `/alunos/{id}` | Busca aluno por ID | - |
| PUT | `/alunos/{id}` | Atualiza aluno | `AlunoRequestDTO` |
| DELETE | `/alunos/{id}` | Remove aluno e dependências | - |
| PUT | `/alunos/{id}/perfil` | Atualiza perfil do aluno | `AtualizarPerfilDTO` |
| PUT | `/alunos/{id}/foto` | Salva foto em base64 | `{ "fotoBase64": "..." }` |

### Professores

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/professores` | Cria professor | `ProfessorRequestDTO` |
| GET | `/professores` | Lista professores | - |
| GET | `/professores/{id}` | Busca professor por ID | - |
| PUT | `/professores/{id}` | Atualiza professor | `ProfessorRequestDTO` |
| DELETE | `/professores/{id}` | Remove professor | - |
| PUT | `/professores/{id}/perfil` | Atualiza perfil | `AtualizarPerfilDTO` |
| PUT | `/professores/{id}/foto` | Salva foto em base64 | `{ "fotoBase64": "..." }` |

Ao remover professor, as disciplinas vinculadas têm o professor definido como `null` antes da exclusão.

### Turmas

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/turmas` | Cria turma | `Turma` |
| GET | `/turmas` | Lista turmas | - |
| GET | `/turmas/{id}` | Busca turma por ID | - |
| PUT | `/turmas/{id}` | Atualiza turma | `Turma` |
| DELETE | `/turmas/{id}` | Remove turma | - |

### Disciplinas

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/disciplinas` | Cria disciplina | `DisciplinaRequestDTO` |
| POST | `/disciplinas/bulk` | Cria a mesma disciplina para várias turmas | `DisciplinaBulkRequestDTO` |
| GET | `/disciplinas` | Lista disciplinas ativas | - |
| GET | `/disciplinas/minhas` | Lista disciplinas conforme usuário logado | - |
| GET | `/disciplinas/{id}` | Busca disciplina se o usuário tiver permissão | - |
| PUT | `/disciplinas/{id}` | Atualiza disciplina | `DisciplinaRequestDTO` |
| DELETE | `/disciplinas/{id}` | Desativa disciplina | - |
| GET | `/disciplinas/turma/{turmaId}` | Lista disciplinas da turma permitida | - |
| GET | `/disciplinas/professor/{professorId}` | Lista disciplinas do professor | - |

Excluir disciplina não apaga do banco; apenas muda `ativa` para `false`.

### Horários

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/horarios` | Cria horário de aula | `HorarioAulaRequestDTO` |
| GET | `/horarios/disciplina/{disciplinaId}` | Lista horários da disciplina | - |
| GET | `/horarios/turma/{turmaId}` | Lista horários da turma | - |
| PUT | `/horarios/{id}` | Atualiza horário | `HorarioAulaRequestDTO` |
| DELETE | `/horarios/{id}` | Remove horário | - |

Body esperado:

```json
{
  "disciplinaId": "uuid",
  "diaSemana": "MONDAY",
  "horaInicio": "08:00",
  "horaFim": "10:00"
}
```

### Aulas

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/aulas/iniciar` | Inicia aula ativa | `{ "disciplinaId": "uuid", "titulo": "..." }` |
| PATCH | `/aulas/{id}/encerrar` | Encerra aula | - |
| GET | `/aulas/disciplina/{disciplinaId}` | Lista aulas da disciplina | - |

Fluxo:

1. Professor ou gestor inicia aula.
2. Aula fica com `ativa = true`.
3. Presenças podem ser registradas enquanto ativa.
4. Encerramento muda `ativa = false`.

### Presenças

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/presencas/registrar` | Registra presença por QR Code | `{ "qrCode": "alunoId", "aulaId": "uuid" }` |
| GET | `/presencas/aluno/{id}` | Lista presenças do aluno | - |
| GET | `/presencas/disciplina/{id}` | Lista presenças da disciplina | - |
| GET | `/presencas/disciplina/{id}/alunos` | Resumo de presença por aluno na disciplina | - |
| GET | `/presencas/aula/{id}` | Lista presenças da aula | - |
| GET | `/presencas` | Lista todas as presenças | - |
| GET | `/presencas/dashboard` | Dashboard geral do gestor | - |
| GET | `/presencas/dashboard/professor/{professorId}` | Dashboard do professor | - |
| DELETE | `/presencas/{id}` | Remove presença | - |
| POST | `/presencas/sincronizar` | Sincroniza presenças simples | Lista de `SincronizacaoPresencaDTO` |

O registro evita duplicidade com base no par `alunoId + aulaId`.

### QR Code

| Método | Rota | Função |
|---|---|---|
| GET | `/qrcode/aluno/{alunoId}` | Gera PNG com o UUID do aluno |

O QR Code contém o UUID do aluno como texto. A presença usa esse valor em `qrCode`.

### Matrículas

| Método | Rota | Função | Body |
|---|---|---|---|
| POST | `/matriculas` | Cria matrícula aluno-disciplina | `MatriculaRequestDTO` |
| GET | `/matriculas` | Lista matrículas | - |

### Offline

| Método | Rota | Função |
|---|---|---|
| GET | `/offline/bootstrap` | Baixa dados iniciais conforme perfil do usuário |
| POST | `/sync/offline` | Sincroniza mudanças feitas offline |

## 8. DTOs Principais

### LoginRequestDTO

```json
{
  "email": "string",
  "senha": "string"
}
```

### LoginResponseDTO

```json
{
  "token": "string",
  "refreshToken": "string"
}
```

### AlunoRequestDTO

```json
{
  "nome": "string",
  "matricula": "string",
  "email": "string",
  "senha": "string",
  "turmaId": "uuid"
}
```

### ProfessorRequestDTO

```json
{
  "nome": "string",
  "email": "string",
  "senha": "string"
}
```

### DisciplinaRequestDTO

```json
{
  "nome": "string",
  "codigo": "string",
  "turmaId": "uuid",
  "professorId": "uuid"
}
```

### DisciplinaBulkRequestDTO

```json
{
  "nome": "string",
  "codigo": "string",
  "professorId": "uuid",
  "turmaIds": ["uuid"]
}
```

### MatriculaRequestDTO

```json
{
  "alunoId": "uuid",
  "disciplinaId": "uuid"
}
```

### SincronizacaoPresencaDTO

```json
{
  "clientId": "uuid",
  "alunoId": "uuid",
  "aulaId": "uuid",
  "dataHoraLocal": "2026-05-18T10:30:00-03:00"
}
```

## 9. Fluxo de Presença por QR Code

1. Aluno acessa `GET /qrcode/aluno/{alunoId}`.
2. API gera uma imagem PNG de QR Code com o UUID do aluno.
3. Professor inicia aula em `POST /aulas/iniciar`.
4. Professor registra presença em `POST /presencas/registrar`.
5. Body enviado:

```json
{
  "qrCode": "uuid-do-aluno",
  "aulaId": "uuid-da-aula"
}
```

6. `PresencaService`:
   - converte `qrCode` para UUID do aluno;
   - busca aluno;
   - busca aula;
   - valida permissão sobre a disciplina;
   - verifica se a presença já existe;
   - verifica se a aula ainda está ativa;
   - salva presença.

7. Se já existir presença para o mesmo aluno e aula, retorna a presença existente.

## 10. Fluxo de Dashboard

O dashboard é calculado em `PresencaService`.

Para cada disciplina:

1. Busca alunos da turma vinculada.
2. Conta total de aulas da disciplina.
3. Conta em quantas aulas cada aluno esteve presente.
4. Calcula:
   - total de alunos;
   - total de presenças;
   - total de faltas;
   - percentual de presença.

Resposta usa `DashboardDisciplinaDTO`.

## 11. Sincronização Offline

A API tem dois mecanismos relacionados ao offline.

### Bootstrap Offline

Endpoint:

```http
GET /offline/bootstrap
```

Retorna dados iniciais para uso offline.

O conteúdo muda por perfil:

- Gestor: recebe turmas, professores, alunos, disciplinas ativas, aulas, horários e presenças.
- Professor: recebe apenas dados ligados às suas disciplinas, turmas e alunos.
- Aluno: recebe sua turma, disciplinas da turma, professores, aulas, horários, suas presenças e QR Code em base64.

### Sync Offline

Endpoint:

```http
POST /sync/offline
```

Entrada principal: `OfflineSyncRequestDTO`.

Pode sincronizar:

- aulas criadas offline;
- presenças registradas offline;
- encerramentos de aulas;
- criação/edição/exclusão de turmas;
- criação/edição/exclusão de professores;
- criação/edição/exclusão de alunos;
- criação/edição/exclusão de disciplinas.

A resposta `OfflineSyncResponseDTO` retorna:

- mapas `clientId -> serverId`;
- itens criados, editados e deletados;
- lista de erros por item.

Padrão usado:

- `clientId`: identificador temporário criado no dispositivo.
- `serverId`: identificador definitivo gerado pelo servidor.

Esse desenho permite que o aplicativo funcione offline e depois reconcilie os dados com o banco.

Regra de segurança importante: os blocos de CRUD do gestor (`turmas`, `professores`, `alunos`, `disciplinas`) só são processados quando o usuário autenticado tem role `GESTOR`. Professores continuam podendo sincronizar aulas, presenças e encerramentos conforme as permissões existentes.

Quando uma criação offline já foi sincronizada antes, a API usa `clientId` para devolver o `serverId` existente e não duplica o registro. Em criações reais, edições e deleções, a API também publica eventos em tempo real para atualizar outros usuários conectados.

## 12. Repositórios

Os repositories estendem `JpaRepository<Entidade, UUID>`.

Exemplos importantes:

- `UsuarioRepository.findByEmail`
- `AlunoRepository.findByTurmaId`
- `DisciplinaRepository.findByProfessorIdAndAtivaTrue`
- `DisciplinaRepository.findByTurmaIdAndAtivaTrue`
- `AulaRepository.findByDisciplinaId`
- `AulaRepository.findByClientId`
- `PresencaRepository.findByAlunoIdAndAulaId`
- `PresencaRepository.findByClientId`
- `PresencaRepository.countAulasPresentesPorAlunoDisciplina`
- `HorarioAulaRepository.findByDisciplinaIdIn`

Os nomes dos métodos seguem convenções do Spring Data JPA, então o Spring monta as queries automaticamente. Quando necessário, o código usa `@Query` JPQL.

## 13. Tratamento de Erros

Arquivo: `GlobalExceptionHandler.java`

Tratamentos:

- `ResponseStatusException`: retorna o status definido e a mensagem.
- `RuntimeException`: retorna `400 Bad Request` com a mensagem.

Também existe `ApiException`, que estende `RuntimeException`.

## 14. Swagger/OpenAPI

Dependência:

```xml
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0
```

Rotas liberadas:

- `/swagger-ui/**`
- `/v3/api-docs/**`

Configuração customizada:

```java
new Server().url("https://unicheckapi-production.up.railway.app")
```

## 15. Tempo Real com WebSocket

A API usa STOMP sobre WebSocket nativo, sem SockJS.

Endpoint:

```text
/ws-native
```

O app conecta usando:

```text
wss://<host-da-api>/ws-native
```

### Autenticação no WebSocket

O JWT vai no header STOMP `Authorization` durante o frame `CONNECT`:

```text
Authorization: Bearer <token>
```

`WebSocketConfig` valida esse JWT com `JwtService`. Se o token estiver ausente ou inválido, a conexão é recusada.

### Prefixos STOMP

- Cliente para servidor: `/app`
- Tópicos públicos da aplicação: `/topic`
- Mensagens privadas do usuário: `/user/queue`

Health-check implementado:

```text
/app/realtime.ping -> /user/queue/pong
```

### Eventos Publicados

Os eventos são leves. Eles não carregam dados sensíveis; apenas avisam que alguma entidade mudou para o app refazer o fetch.

Formato:

```json
{
  "tipo": "PRESENCA",
  "entidade": "ALUNO",
  "id": "uuid",
  "timestamp": "2026-05-25T16:12:00"
}
```

Principais tópicos:

- `/topic/gestor`
- `/topic/disciplina/{id}`
- `/topic/turma/{id}`
- `/topic/aluno/{id}`

Exemplos de eventos:

- `AULA_INICIADA`
- `AULA_ENCERRADA`
- `PRESENCA`
- `PRESENCA_REMOVIDA`
- `TURMA_CRIADA`, `TURMA_ATUALIZADA`, `TURMA_DELETADA`
- `ALUNO_CRIADO`, `ALUNO_ATUALIZADO`, `ALUNO_DELETADO`
- `PROFESSOR_CRIADO`, `PROFESSOR_ATUALIZADO`, `PROFESSOR_DELETADO`
- `DISCIPLINA_CRIADA`, `DISCIPLINA_ATUALIZADA`, `DISCIPLINA_DELETADA`

Regra importante: falha ao publicar evento WebSocket não deve quebrar a transação REST. Por isso o publisher envolve o envio em `try/catch`.

## 16. Docker

O `Dockerfile` usa build em duas etapas:

1. Imagem `eclipse-temurin:21-jdk` para compilar com Maven.
2. Imagem `eclipse-temurin:21-jre` para executar o JAR.

Comandos principais no Dockerfile:

```dockerfile
RUN ./mvnw -B dependency:go-offline
RUN ./mvnw -B clean package -DskipTests
ENTRYPOINT ["java", "-jar", "app.jar"]
```

A aplicação expõe a porta `8080`.

## 17. Como Executar Localmente

1. Criar `.env` com base em `.env.example`.
2. Configurar banco PostgreSQL.
3. Executar:

```powershell
.\mvnw.cmd spring-boot:run
```

Ou gerar o JAR:

```powershell
.\mvnw.cmd clean package
java -jar target\Unicheckapi-0.0.1-SNAPSHOT.jar
```

Depois acessar:

```text
http://localhost:8080/swagger-ui/index.html
```

## 18. Build e Verificação

Build executado antes do deploy:

```powershell
.\mvnw.cmd clean package
```

Resultado:

```text
BUILD SUCCESS
```

Artefato gerado:

```text
target/Unicheckapi-0.0.1-SNAPSHOT.jar
```

Observação: existe 1 teste no projeto, mas ele está marcado como skipped. Então o build compilou e empacotou a aplicação, mas não houve teste automatizado efetivo rodando.

## 19. Pontos de Atenção Técnicos

1. `UsuarioService` cria e atualiza usuário genérico sem criptografar a senha. Já `AlunoService`, `ProfessorService`, `AuthService` e `DataInitializer` usam BCrypt.
2. `AuthService.register` existe, mas não há endpoint chamando register no controller atual.
3. `AuthService.register` também não define `role`, então um usuário criado por esse fluxo ficaria sem papel.
4. `/usuarios` não tem regra explícita de gestor em `SecurityConfig`; pela regra final, qualquer usuário autenticado acessa, salvo mudança futura.
5. `/matriculas` também cai na regra genérica de apenas autenticado.
6. `POST`, `PUT` e `DELETE` de `/horarios` não têm regra específica em `SecurityConfig`; caem em `anyRequest().authenticated()`.
7. Algumas mensagens/comentários no código aparecem com caracteres quebrados, indicando provável problema de encoding em arquivos antigos.
8. O QR Code contém apenas o UUID do aluno; se o QR for compartilhado, outro professor pode registrar presença para aquele aluno se tiver permissão sobre a aula.
9. A deleção de disciplina é lógica (`ativa=false`), mas turmas, alunos, professores e presenças têm exclusões físicas em alguns fluxos.
10. O CORS permite qualquer origem com credenciais, o que deve ser revisado antes de produção.
11. O refresh token cria uma tabela nova via JPA/Hibernate quando `ddl-auto=update` estiver ativo. Não há arquivo SQL manual nesta alteração.
12. O WebSocket está liberado no filtro HTTP, mas autentica o usuário no frame STOMP `CONNECT`.

## 20. Mapa Mental do Sistema

```text
Usuario
├── Aluno
│   ├── pertence a Turma
│   ├── possui QR Code
│   └── gera Presencas
├── Professor
│   └── ministra Disciplinas
└── Gestor
    └── administra cadastros

Turma
└── possui Alunos
└── possui Disciplinas

Disciplina
├── pertence a Turma
├── tem Professor
├── tem Horarios
└── tem Aulas

Aula
├── pertence a Disciplina
├── pode estar ativa ou encerrada
└── recebe Presencas

Presenca
├── pertence a Aluno
├── pertence a Aula
└── também guarda Disciplina
```

## 21. Resumo da Construção

A API foi construída como uma aplicação REST de gestão acadêmica com autenticação JWT e autorização por perfil. O domínio central gira em torno de turmas, disciplinas, aulas e presenças. A presença é registrada com QR Code do aluno dentro de uma aula ativa, e o sistema calcula dashboards com total de presenças, faltas e percentual.

O projeto também tem uma parte importante de operação offline: o cliente pode baixar um pacote inicial de dados, trabalhar sem conexão e depois enviar mudanças para o servidor usando `clientId` para reconciliar registros criados localmente com UUIDs definitivos do banco.

Na versão atual, a API também suporta sessão persistente com refresh token e atualização em tempo real via WebSocket. Isso permite que o app mantenha o usuário logado por uma janela offline maior e atualize telas quando aulas, presenças ou cadastros forem alterados por outro usuário.

Como material de estudo, os arquivos mais importantes para ler em sequência são:

1. `model/Usuario.java`, `Aluno.java`, `Professor.java`, `Turma.java`, `Disciplina.java`, `Aula.java`, `Presenca.java`.
2. `security/SecurityConfig.java`, `JwtAuthenticationFilter.java`, `JwtService.java`.
3. `Controller/AuthController.java`, `AulaController.java`, `PresencaController.java`, `DisciplinaController.java`.
4. `service/DisciplinaService.java`, `AulaService.java`, `PresencaService.java`, `OfflineBootstrapService.java`, `OfflineSyncService.java`.
5. `repository/*Repository.java`.
6. `dto/*DTO.java`.
