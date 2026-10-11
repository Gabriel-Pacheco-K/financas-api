# 💰 Finanças API

![CI](https://github.com/Gabriel-Pacheco-K/financas-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

API REST para controle de finanças pessoais. Cada usuário cadastra suas categorias de receita e despesa, registra transações e consulta um **relatório mensal** com saldo, taxa de poupança e gastos por categoria.

O foco do projeto é mostrar práticas usadas em APIs de produção: autenticação stateless com JWT, isolamento de dados entre usuários, migrações versionadas, tratamento de erros padronizado, testes automatizados e integração contínua.

---

## ✨ Funcionalidades

- **Autenticação JWT**: cadastro e login com senha criptografada (BCrypt)
- **Categorias** de receita (`INCOME`) ou despesa (`EXPENSE`), sem nomes duplicados por usuário
- **Transações** com CRUD completo, filtros por período, categoria e tipo, além de paginação e ordenação
- **Relatório mensal** com total de receitas, total de despesas, saldo, taxa de poupança e ranking por categoria
- **Isolamento de dados**: um usuário nunca acessa dados de outro (a API responde 404, sem revelar que o recurso existe)
- **Regras de negócio**: uma categoria com transações não pode ser removida nem mudar de tipo
- **Erros no padrão RFC 7807** (Problem Details), com a lista de campos inválidos
- **Documentação interativa** com Swagger / OpenAPI

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (records, text blocks) |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation, Security, Actuator) |
| Banco de dados | PostgreSQL 16 + Flyway |
| Segurança | Spring Security + JWT (jjwt) + BCrypt |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, H2 |
| Infra | Docker (multi-stage), Docker Compose, GitHub Actions |

## 🏗️ Arquitetura

```
Cliente ──HTTP──▶ JwtAuthenticationFilter ──▶ Controller ──▶ Service ──▶ Repository ──▶ PostgreSQL
                  (valida o token)            (HTTP/DTOs)    (regras)    (JPA/Specs)
```

```
src/main/java/com/gabrielpacheco/financas
├── controller   # Endpoints REST e documentação OpenAPI
├── service      # Regras de negócio e transações
├── repository   # Spring Data JPA + Specifications para filtros dinâmicos
├── domain       # Entidades JPA
├── dto          # Records de entrada e saída (a API nunca expõe entidades)
├── security     # JWT, filtro de autenticação e configuração do Spring Security
├── exception    # Exceções de domínio e handler global (Problem Details)
└── config       # OpenAPI e paginação
```

**Decisões técnicas:**

- **DTOs com `record`**: as entidades nunca saem da API, o que evita vazar dados (como o hash da senha) e problemas de lazy loading.
- **`Specification` para filtros**: os filtros opcionais são combinados de forma dinâmica, sem escrever uma query para cada combinação.
- **Agregação no banco**: o relatório usa `GROUP BY` em JPQL. O banco soma os valores, e a aplicação não precisa carregar todas as transações.
- **`BigDecimal` para dinheiro**: evita os erros de arredondamento de `double`.
- **Flyway + `ddl-auto: validate`**: o schema é versionado em SQL, e o Hibernate apenas valida o mapeamento.
- **Índice em `(user_id, date)`**: cobre a consulta mais comum, que é a de transações do usuário em um período.

## 🚀 Como executar

### Com Docker (recomendado)

```bash
git clone https://github.com/Gabriel-Pacheco-K/financas-api.git
cd financas-api
docker compose up --build
```

A API sobe em `http://localhost:8080`, e a documentação fica em **http://localhost:8080/swagger-ui.html**.

### Localmente

Requisitos: Java 21, Maven e um PostgreSQL rodando.

```bash
docker compose up -d db   # ou use um PostgreSQL próprio
mvn spring-boot:run
```

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/financas` | URL do banco |
| `DB_USER` / `DB_PASSWORD` | `financas` | Credenciais do banco |
| `JWT_SECRET` | *(valor de desenvolvimento)* | Segredo do token (mín. 32 caracteres) |
| `JWT_EXPIRATION` | `2h` | Validade do token |

## 📖 Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Cria uma conta e retorna o token |
| `POST` | `/api/auth/login` | Faz login e retorna o token |
| `GET` | `/api/categories` | Lista as categorias |
| `POST` | `/api/categories` | Cria uma categoria |
| `PUT` | `/api/categories/{id}` | Atualiza uma categoria |
| `DELETE` | `/api/categories/{id}` | Remove uma categoria sem transações |
| `GET` | `/api/transactions` | Lista as transações (`start`, `end`, `categoryId`, `type`, `page`, `size`, `sort`) |
| `GET` | `/api/transactions/{id}` | Busca uma transação |
| `POST` | `/api/transactions` | Registra uma transação |
| `PUT` | `/api/transactions/{id}` | Atualiza uma transação |
| `DELETE` | `/api/transactions/{id}` | Remove uma transação |
| `GET` | `/api/reports/monthly?month=2026-10` | Resumo do mês |

### Exemplo de uso

```bash
# 1. Criar conta
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Gabriel","email":"gabriel@email.com","password":"senha-segura"}'
# => {"token":"eyJhbGciOi...","type":"Bearer","expiresInSeconds":7200}

# 2. Usar o token nas demais rotas
curl localhost:8080/api/reports/monthly?month=2026-10 -H "Authorization: Bearer <token>"
```

Resposta do relatório:

```json
{
  "month": "2026-10",
  "totalIncome": 4200.00,
  "totalExpense": 650.40,
  "balance": 3549.60,
  "savingsRate": 84.51,
  "byCategory": [
    { "category": "Salário", "type": "INCOME",  "total": 4200.00 },
    { "category": "Mercado", "type": "EXPENSE", "total": 650.40 }
  ]
}
```

Exemplo de erro de validação:

```json
{
  "type": "about:blank",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "errors": { "amount": "deve ser maior que ou igual à 0.01" }
}
```

## 🧪 Testes

```bash
mvn verify
```

São 15 testes:

- **Unitários** (Mockito): cálculo do relatório (saldo, taxa de poupança, mês sem receita) e regras das categorias
- **Integração** (MockMvc + H2): o fluxo completo pela API, cobrindo autenticação, validação, filtros, paginação, relatório e isolamento entre usuários

O GitHub Actions executa os testes e o build da imagem Docker a cada push.

## 🗺️ Próximos passos

- [ ] Orçamento mensal por categoria, com alerta ao ultrapassar o limite
- [ ] Transações recorrentes
- [ ] Exportação em CSV
- [ ] Testcontainers para testar com PostgreSQL real

---

Desenvolvido por **Gabriel Pacheco Kiesel**: [Portfólio](https://gabriel-pacheco-k.github.io) · [LinkedIn](https://www.linkedin.com/in/gabriel-pacheco-3b637a362/) · [GitHub](https://github.com/Gabriel-Pacheco-K)
