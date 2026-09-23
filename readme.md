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