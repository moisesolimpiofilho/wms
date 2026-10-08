# Auth Service — WMS UNESP

Microsserviço de autenticação baseado em **JWT** para o sistema **WMS (Warehouse Management System)** da UNESP.

Fornece endpoints de registro, login e validação de identidade, sendo consumido pelos demais serviços do WMS através de tokens JWT.

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![JWT](https://img.shields.io/badge/JWT-jjwt%200.12.6-black)
![License](https://img.shields.io/badge/license-none-lightgrey)

---

## 📋 Sobre o projeto

O **auth** é o serviço responsável por toda a camada de autenticação do sistema WMS. Ele:

- Registra novos usuários com senha criptografada (**BCrypt**)
- Autentica credenciais e emite um **token JWT** (válido por 1 hora)
- Valida tokens em requisições protegidas via filtro customizado
- Expõe a identidade do usuário autenticado para os demais serviços

---

## 🛠️ Stack técnica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 25 | Linguagem |
| Spring Boot | 4.1.1 | Framework principal |
| Spring Web MVC | — | Endpoints REST |
| Spring Security | — | Autenticação / autorização |
| Spring Data JPA | — | Persistência |
| PostgreSQL | — | Banco de dados |
| jjwt (api/impl/jackson) | 0.12.6 | Geração e validação de JWT |
| Lombok | — | Redução de boilerplate |
| Maven | — | Build |

---

## 📁 Estrutura do projeto

```
src/main/java/br/unesp/wms/auth/
├── config/
│   └── SecurityConfig.java          # Configuração do Spring Security
├── controller/
│   └── AuthController.java          # Endpoints /auth/*
├── entity/
│   └── User.java                    # Entidade JPA "users"
├── repository/
│   └── UserRepository.java          # Acesso ao banco
└── security/
    ├── JwtAuthenticationFilter.java # Filtro que valida o Bearer token
    └── JwtService.java              # Geração e parse do JWT
```

---

## ⚙️ Configuração

As configurações ficam em `src/main/resources/application.properties` e podem ser sobrescritas via **variáveis de ambiente**.

### Variáveis suportadas

| Variável | Default | Descrição |
|---|---|---|
| `server.port` | `8082` | Porta da aplicação |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/meudb` | URL do PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `admin` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `senha1234` | Senha do banco |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Host do Redis *(reservado)* |
| `SPRING_DATA_REDIS_PORT` | `6379` | Porta do Redis *(reservado)* |
| `SPRING_DATA_REDIS_PASSWORD` | `senha1234` | Senha do Redis *(reservado)* |
| `jwt.secret` | *(ver properties)* | Chave Base64 HMAC-SHA |
| `jwt.expiration` | `3600000` | Expiração do token em ms (1h) |

> ⚠️ **Produção:** sempre sobrescreva `jwt.secret`, `SPRING_DATASOURCE_*` e `SPRING_DATA_REDIS_PASSWORD` via variáveis de ambiente. Nunca versione segredos reais.

---

## 🚀 Como rodar

### Pré-requisitos

- **JDK 25** ([download](https://jdk.java.net/))
- **Maven 3.9+** (ou use o wrapper `./mvnw`)
- **PostgreSQL** rodando em `localhost:5432` com um banco chamado `meudb`

## 🔌 Endpoints

Base URL: `http://localhost:8082`

### `POST /auth/register` — Registrar usuário

**Body:**
```json
{
  "username": "joao",
  "password": "senhaSegura123"
}
```

**Resposta:** `200 OK` (sem corpo)

---

### `POST /auth/login` — Autenticar e obter token

**Body:**
```json
{
  "username": "joao",
  "password": "senhaSegura123"
}
```

**Sucesso — `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Falha — `401 Unauthorized`:**
```json
{
  "status": 401,
  "message": "Usuário ou senha inválidos"
}
```

---

### Exemplos com `curl`

```bash
# Registrar
curl -X POST http://localhost:8082/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"joao","password":"senhaSegura123"}'

# Login
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"joao","password":"senhaSegura123"}'
```

---

## 🔐 Fluxo de autenticação

```
┌────────┐         ┌─────────────┐        ┌──────────────┐
│ Client │──(1)──▶ │ AuthService │──(2)──▶ │ UserRepository│
│        │         │             │        │  (PostgreSQL) │
│        │◀──(3)── │  JWT gerado │        └──────────────┘
└────────┘         └─────────────┘
    │
    │  (4) Guarda o token e envia no header Authorization
    ▼
┌──────────────────────────────────────────────┐
│ Requisição para endpoint protegido           │
│ Header: Authorization: Bearer <token>        │
└──────────────────────────────────────────────┘
    │
    ▼
┌────────────────────────────┐
│ JwtAuthenticationFilter    │
│  ├─ extrai o token         │
│  ├─ valida assinatura/exp. │
│  └─ popula SecurityContext │
└────────────────────────────┘
    │
    ▼
┌────────────────────────────┐
│ Controller acessa via      │
│ Authentication.getName()   │
└────────────────────────────┘
```

**Detalhes:**

- A aplicação é **stateless** — nenhuma sessão HTTP é mantida.
- O token JWT carrega apenas o `subject` (username), com expiração de 1 hora.
- Requisições sem `Authorization: Bearer ...` seguem sem autenticação e são barradas pelo `AuthorizationFilter` do Spring Security.

---

## 📌 Observações e limitações conhecidas

- **Retorno padrão de 403 em vez de 401:** sem um `AuthenticationEntryPoint` configurado, requisições não autenticadas a endpoints protegidos retornam **403** (corpo vazio) em vez do JSON de 401 esperado. Para customizar, registre um `AuthenticationEntryPoint` + `AccessDeniedHandler` no `SecurityConfig`.
- **Redis ainda não utilizado:** há propriedades de conexão configuradas e dependências previstas, mas o Redis **não é usado** nesta versão (reservado para futura blacklist/refresh tokens).
- **Sem roles/permissões:** todos os usuários autenticados têm o mesmo nível de acesso.
- **Sem validação de entrada:** não há Bean Validation (`@NotBlank`, `@Size`, etc.) nos payloads — campos ausentes ou vazios podem causar comportamento inesperado.
- **Sem testes automatizados:** os starters de teste estão no `pom.xml`, mas nenhum teste foi implementado ainda.

---
