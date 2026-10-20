# 🚀 Ambiente de Banco de Dados Local (Docker Compose)

Este repositório contém a configuração do Docker Compose para subir os serviços de infraestrutura local necessários para o desenvolvimento da aplicação **Spring Boot**:

* **PostgreSQL**: Banco de dados relacional (porta `5432`).
* **MongoDB**: Banco de dados orientado a documentos (porta `27017`).
* **Redis**: Armazenamento em memória para cache e processamento de filas (porta `6379`).

---

## 📋 Pré-requisitos

Certifique-se de ter as seguintes ferramentas instaladas em sua máquina:

* [Docker Engine](https://docs.docker.com/get-docker/) (v20.10+)
* [Docker Compose](https://docs.docker.com/compose/install/) (v2.0+)

---

## 🛠️ Como Executar

### 1. Iniciar os Containers

Navegue até a pasta raiz do projeto onde se encontra o arquivo `docker-compose.yml` e execute:

```bash
docker compose up -d
```

### 2. Verificar o Status dos Containers

Para confirmar se todos os serviços estão ativos e rodando sem erros:

```bash
docker compose ps
```

Você deverá ver os containers `postgres_db`, `mongo_db` e `redis_cache` com o status `Up` ou `running`.

---

## 🔑 Dados de Acesso aos Bancos

Abaixo estão os parâmetros de conexão para configurar em ferramentas de banco de dados (ex: DBeaver, MongoDB Compass, Redis Insight) ou no arquivo `application.properties` da aplicação Spring Boot:

| Serviço | Host | Porta | Usuário | Senha | Banco de Dados / Auth |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL** | `localhost` | `5432` | `admin` | `senha1234` | `meudb` |
| **MongoDB** | `localhost` | `27017` | `admin` | `senha1234` | `meudb` *(Auth Source: `admin`)* |
| **Redis** | `localhost` | `6379` | — | `senha1234` | Index: `0` |

---

## 🧪 Testando as Conexões via Terminal

Você pode testar a conectividade de cada banco executando os utilitários de linha de comando diretamente dentro dos containers Docker:

### 1. PostgreSQL (`psql`)
```bash
docker exec -it postgres_db psql -U admin -d meudb
```
* Digite a senha: `senha1234`
* Para testar, execute: `SELECT version();`
* Para sair: `\q`

### 2. MongoDB (`mongosh`)
```bash
docker exec -it mongo_db mongosh -u admin -p senha1234 --authenticationDatabase admin
```
* Para testar, crie um documento:
  ```javascript
  use meudb
  db.teste.insertOne({ mensagem: "Conexao OK!", data: new Date() })
  db.teste.find()
  ```
* Para sair: `exit`

### 3. Redis (`redis-cli`)
```bash
docker exec -it redis_cache redis-cli -a senha1234
```
* Para testar o cache/fila:
  ```text
  PING
  SET teste "OK"
  GET teste
  ```
* Para sair: `quit`

---

## ⚙️ Configuração no Spring Boot

Insira as seguintes propriedades no arquivo `src/main/resources/application.properties` para conectar sua aplicação Java rodando em `localhost`:

```properties
# --- PostgreSQL ---
spring.datasource.url=jdbc:postgresql://localhost:5432/meudb
spring.datasource.username=admin
spring.datasource.password=senha1234
spring.jpa.hibernate.ddl-auto=update

# --- MongoDB ---
spring.data.mongodb.uri=mongodb://admin:senha1234@localhost:27017/meudb?authSource=admin

# --- Redis ---
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=senha1234
```

---

## 📦 Redis no `warehouse_module`

O `RedisTransactionStore` grava cada `TransactionLog` em `warehouse:transactions:<transactionId>`, sem expiração, e permite recuperá-lo pelo identificador. O identificador é tratado como único no Redis; tentar gravar uma transação com chave existente gera erro em vez de substituir o registro.

O `EtiquetaService` oferece operações de entrada, saída parcial e consulta de etiquetas por código. Entradas inicializam o saldo disponível e geram movimentação e transação; saídas rejeitam saldo insuficiente e desativam a etiqueta quando o saldo chega a zero. Os testes unitários mockam os repositórios JPA e o `RedisTransactionStore`.

Para executar os testes unitários dos serviços:

```bash
mvn -f warehouse_module/pom.xml test
```

## Auditoria dos CRUDs

O endpoint `GET /api/logs-transacao` é somente leitura. Os eventos não são criados,
alterados ou removidos manualmente: cada criação, atualização e exclusão lógica feita
pelos CRUDs gera automaticamente um registro persistente na tabela `audit_log`, na mesma
transação da operação original. Cada registro informa a entidade, o ID, a operação, o
usuário associado e a data/hora, além dos estados anterior e atual em JSON quando
aplicável. `GET /api/logs-transacao/{id}` consulta um evento individual.

## Consulta de saldos de estoque

Os endpoints `GET /api/estoques` e `GET /api/estoques/{id}` são somente leitura.
Os saldos são criados e atualizados pelos fluxos operacionais, como recebimentos,
movimentações e ajustes de inventário; não podem ser alterados diretamente pela API
de consulta.

---

## 🛑 Gerenciamento do Ambiente

### Visualizar Logs
```bash
# Todos os containers
docker compose logs -f

# Apenas um container específico (ex: postgres)
docker compose logs -f postgres
```

### Parar os Containers (Sem perder dados)
```bash
docker compose stop
```

### Reiniciar os Containers
```bash
docker compose start
```

### Destruir Containers e Resetar Dados
```bash
docker compose down -v
```
> ⚠️ **Atenção:** O comando `docker compose down -v` apaga permanentemente todos os dados armazenados nos volumes do Postgres, MongoDB e Redis localmente.

## Integração de ordens de estoque via Redis Pub/Sub

O módulo `warehouse_module` assina o tópico Redis `warehouse:orders` (configurável por
`warehouse.redis.orders.topic`). Para simular uma aplicação externa, envie uma ordem para
`POST http://localhost:8083/api/integracao/ordens-estoque`. O endpoint publica a mensagem no
tópico e responde `202 Accepted` quando há consumidores inscritos; o processamento é assíncrono.

Exemplo de entrada:

```bash
curl -X POST http://localhost:8083/api/integracao/ordens-estoque \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "APP-EXT-ENT-001",
    "movementType": "ENTRADA",
    "productId": 1,
    "batchId": 1,
    "locationId": 1,
    "quantity": 10.0000,
    "weight": 2.5000,
    "reason": "RECEBIMENTO_AQUISICAO",
    "userId": 1,
    "userName": "integracao-externa",
    "observation": "Entrada solicitada pelo sistema externo"
  }'
```

Os motivos são fixos e podem ser consultados em `GET
http://localhost:8083/api/motivos-movimentacao`. O endpoint retorna os códigos aceitos no campo
`reason` e suas descrições. Os valores disponíveis são `RECEBIMENTO_PRODUCAO`,
`RECEBIMENTO_AQUISICAO`, `EXPEDICAO_VENDA`, `EXPEDICAO_INVENTARIO` e
`AJUSTE_INVENTARIO`.
O contrato agora usa `reason` com um desses códigos, em vez do antigo `reasonId`.

## Ajuste de estoque por inventário

Depois de contar um item de inventário, aplique a contagem com
`POST /api/inventario-itens/{id}/ajustes`. Produto, lote e local do corpo devem corresponder
ao item. O processamento bloqueia o saldo atual, preserva quantidades reservadas, atualiza o
estoque e o cache Redis e, quando há divergência, registra movimentação do tipo `AJUSTE`,
`HistoricoAjuste` e `TransactionLog`.

```json
{
  "productId": 1,
  "locationId": 4,
  "batchId": 3,
  "countedQuantity": 8.0000,
  "countedWeight": 12.5000,
  "reason": "AJUSTE_INVENTARIO",
  "userId": 5,
  "userName": "operador"
}
```

`countedWeight` é opcional. Se omitido, o peso existente não é alterado. Quando não há
divergência, a API retorna `adjusted: false` e não cria movimentação ou histórico; repetir uma
contagem já aplicada também não duplica esses registros. Inventários concluídos ou cancelados
não aceitam ajustes, e a contagem não pode ficar abaixo do saldo reservado.

## Recebimento de itens e entrada de estoque

Ao cadastrar um item em `POST /api/recebimento-itens`, informe `locationId`, `userId` e um
motivo de recebimento (`RECEBIMENTO_AQUISICAO` ou `RECEBIMENTO_PRODUCAO`). O item continua
sendo registrado e, na mesma transação, o WMS executa o fluxo de entrada de ordem: cria a
ordem concluída e seu item, cria a movimentação, atualiza o estoque e o cache e grava o log
da transação no Redis. A quantidade recebida é adicionada aos saldos total e disponível; o
peso é atualizado quando informado. O código da ordem é gerado a partir dos IDs do recebimento
e do item (`RECEBIMENTO-<receiptId>-<itemId>`), evitando exigir um código externo. Exemplo:

```json
{
  "receiptId": 1,
  "productId": 2,
  "batchId": 3,
  "locationId": 4,
  "userId": 5,
  "reason": "RECEBIMENTO_AQUISICAO",
  "quantity": 6,
  "weight": 12.5,
  "userNameCreator": "operador",
  "userNameLastChange": "operador"
}
```

`userId` e `reason` são dados de entrada do fluxo e não são persistidos como colunas do item
de recebimento. Para movimentos originados de recebimentos, o log transacional não possui
`sealId`, pois o item recebido não representa uma etiqueta.

Para simular uma saída, use `movementType: "SAIDA"` e os mesmos campos, com o local de
origem em `locationId`. A saída só é executada se houver saldo disponível do produto, lote
e unidade logística informados. Lote e unidade logística são opcionais. O `code` deve ser
único por ordem: publicar novamente uma ordem já processada não repete a movimentação.
Para saídas, `vehiclePlate` também pode ser informado. Após validar e atualizar o estoque e o
cache, o WMS cria a movimentação, a ordem e uma expedição concluída, vinculada ao ID da ordem.
A expedição recebe o código `EXP-<code>`, a data/hora do processamento e a placa informada
(ou `null`, se omitida). Entradas não criam expedição.

Ao consumir uma mensagem válida, o WMS atualiza o saldo, registra a ordem concluída, seu
item, a movimentação, a expedição (para saídas) e o log da transação no Redis. Falhas de
validação ou saldo insuficiente são registradas nos logs do serviço. Redis Pub/Sub oferece entrega efêmera: mensagens
publicadas sem consumidores ativos não ficam armazenadas para processamento posterior.