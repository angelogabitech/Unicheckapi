# FaltaApi.md - Sincronizacao offline completa

Este arquivo registra somente as mudancas da API Java e do banco relacionadas ao plano de offline completo do app mobile.

## Diretorio da API principal

```text
C:\Projetos\ProjetosFaculdade\Unicheck\Unicheckapi-main\Unicheckapiclaude
```

---

## Sincronizacao offline completa de aulas, presencas e encerramentos

**Status:** aplicado na API local de teste em:

```text
C:\Projetos\ProjetosFaculdade\Unicheck\Unicheckapi-main\Unicheckapiclaude
```

### Problema

O app mobile conseguia salvar presencas offline, mas o endpoint existente:

```text
POST /presencas/sincronizar
```

aceitava apenas:

```json
[
  { "alunoId": "uuid", "aulaId": "uuid", "dataHoraLocal": "..." }
]
```

Isso funciona somente quando a aula ja existe na API. Se o professor iniciar aula offline, o app cria uma aula local com ID local, e a API nao conhece esse `aulaId`.

### Regra implementada

Criado endpoint novo:

```text
POST /sync/offline
```

Roles permitidas:

```text
PROFESSOR, GESTOR
```

Fluxo esperado:

1. API recebe aulas criadas offline com `clientId`.
2. API cria a aula real ou reutiliza a aula ja sincronizada pelo mesmo `clientId`.
3. API recebe presencas com `aulaClientId` e relaciona com a aula real.
4. API recebe encerramentos com `aulaClientId` e encerra a aula real.
5. API retorna mapas `clientId -> serverId` para o app limpar a fila local.
6. API retorna erros por item sem derrubar o pacote inteiro.

### Contrato do request

```json
{
  "aulas": [
    {
      "clientId": "uuid-gerado-no-app",
      "disciplinaId": "uuid",
      "titulo": "Aula offline",
      "dataHoraLocal": "2026-05-04T10:00:00"
    }
  ],
  "presencas": [
    {
      "clientId": "uuid-gerado-no-app",
      "alunoId": "uuid",
      "aulaId": null,
      "aulaClientId": "uuid-gerado-no-app-da-aula",
      "dataHoraLocal": "2026-05-04T10:05:00"
    }
  ],
  "encerramentos": [
    {
      "clientId": "uuid-gerado-no-app",
      "aulaId": null,
      "aulaClientId": "uuid-gerado-no-app-da-aula",
      "dataHoraLocal": "2026-05-04T11:00:00"
    }
  ]
}
```

### Contrato do response

```json
{
  "aulas": [
    { "clientId": "uuid-local", "serverId": "uuid-api" }
  ],
  "presencas": [
    { "clientId": "uuid-local", "serverId": "uuid-api" }
  ],
  "encerramentos": [
    { "clientId": "uuid-local", "serverId": "uuid-api" }
  ],
  "erros": [
    {
      "clientId": "uuid-local",
      "tipo": "PRESENCA",
      "mensagem": "Aluno nao encontrado"
    }
  ]
}
```

### API - Arquivos adicionados

```text
src\main\java\com\unicheck\Unicheckapi\Controller\OfflineSyncController.java
src\main\java\com\unicheck\Unicheckapi\service\OfflineSyncService.java
src\main\java\com\unicheck\Unicheckapi\dto\OfflineAulaSyncDTO.java
src\main\java\com\unicheck\Unicheckapi\dto\OfflinePresencaSyncDTO.java
src\main\java\com\unicheck\Unicheckapi\dto\OfflineEncerramentoAulaSyncDTO.java
src\main\java\com\unicheck\Unicheckapi\dto\OfflineSyncRequestDTO.java
src\main\java\com\unicheck\Unicheckapi\dto\OfflineSyncResponseDTO.java
src\main\java\com\unicheck\Unicheckapi\dto\OfflineSyncMapDTO.java
src\main\java\com\unicheck\Unicheckapi\dto\OfflineSyncErroDTO.java
```

### API - Arquivos alterados

```text
src\main\java\com\unicheck\Unicheckapi\model\Aula.java
src\main\java\com\unicheck\Unicheckapi\model\Presenca.java
src\main\java\com\unicheck\Unicheckapi\repository\AulaRepository.java
src\main\java\com\unicheck\Unicheckapi\repository\PresencaRepository.java
src\main\java\com\unicheck\Unicheckapi\security\SecurityConfig.java
src\main\java\com\unicheck\Unicheckapi\service\PresencaService.java
```

### API - Pontos principais implementados

`Aula.java` recebeu:

```java
@Column(name = "client_id", unique = true)
private UUID clientId;
```

`Presenca.java` recebeu:

```java
@Column(name = "client_id", unique = true)
private UUID clientId;
```

`Presenca.prePersist()` foi ajustado para preservar `dataHoraLocal`:

```java
@PrePersist
public void prePersist() {
    if (dataHora == null) {
        dataHora = LocalDateTime.now();
    }
}
```

`AulaRepository.java` recebeu:

```java
Optional<Aula> findByClientId(UUID clientId);
```

`PresencaRepository.java` recebeu:

```java
Optional<Presenca> findByClientId(UUID clientId);
Optional<Presenca> findByAlunoIdAndAulaId(UUID alunoId, UUID aulaId);
```

`SecurityConfig.java` recebeu:

```java
.requestMatchers("/sync/offline").hasAnyRole("PROFESSOR", "GESTOR")
```

`PresencaService.java` foi ajustado para:

```java
.disciplina(aula.getDisciplina())
.dataHora(dto.getDataHoraLocal())
```

na sincronizacao legada de presencas.

### Banco - Alteracoes necessarias no Supabase

Adicionar identificadores idempotentes vindos do app:

```sql
ALTER TABLE aulas
ADD COLUMN IF NOT EXISTS client_id UUID;

ALTER TABLE presencas
ADD COLUMN IF NOT EXISTS client_id UUID;
```

Criar indices unicos para evitar duplicidade ao reprocessar a mesma fila offline:

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_aulas_client_id
ON aulas(client_id)
WHERE client_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_presencas_client_id
ON presencas(client_id)
WHERE client_id IS NOT NULL;
```

Recomendado tambem bloquear duplicidade de presenca por aluno e aula:

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_presencas_aluno_aula
ON presencas(aluno_id, aula_id);
```

### Observacao de compatibilidade

O endpoint antigo:

```text
POST /presencas/sincronizar
```

continua existindo para compatibilidade, mas o app mobile preparado para offline completo deve usar:

```text
POST /sync/offline
```
