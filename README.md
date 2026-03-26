# Inventory Service

Serviço de gerenciamento de estoque da Anymarket, responsável por reservar estoque a partir de eventos de pedidos, notificar alertas de estoque baixo e garantir consistência via comunicação assíncrona orientada por eventos.

## 📋 Visão Geral

- **Linguagem**: Java 21
- **Framework**: Quarkus 3.34.1
- **Banco de Dados**: PostgreSQL 18
- **Message Broker**: Apache Kafka (SmallRye Reactive Messaging)
- **Build**: Maven

---

## 🚀 Como Subir a Infraestrutura

### Pré-requisitos

- Docker e Docker Compose instalados
- Git

### Subir os Serviços com Docker Compose

A infraestrutura local é composta por PostgreSQL. Kafka é provido pelo docker-compose do ecossistema (order-service).

```bash
# Subir todos os serviços em background
docker-compose up -d

# Visualizar logs em tempo real
docker-compose logs -f

# Parar todos os serviços
docker-compose down

# Parar e remover volumes (apaga dados)
docker-compose down -v
```

### Verificar Status dos Serviços

```bash
# Listar containers em execução
docker-compose ps

# Verificar saúde do PostgreSQL
docker-compose logs postgres | grep healthcheck
```

### Serviços Disponíveis

| Serviço | Container | Porta | Credenciais |
|---------|-----------|-------|-------------|
| **PostgreSQL** | pg-inventory | 5444 | `user: pg-inventory` / `pass: 0706` |

---

## 🏃 Como Executar o Serviço

### Pré-requisitos

- Java 21 JDK instalado
- Maven 3.8+
- Infraestrutura em execução (Docker Compose)
- Kafka acessível em `localhost:9092`

### Build do Projeto

```bash
# Compilar o projeto
./mvnw clean package

# Compilar sem executar testes
./mvnw clean package -DskipTests
```

### Executar o Serviço

```bash
# Modo dev (hot reload)
./mvnw quarkus:dev

# Ou após build
java -jar target/quarkus-app/quarkus-run.jar
```

O serviço estará disponível em **http://localhost:4545**

> **Dev UI** disponível em modo dev em http://localhost:4545/q/dev/

## 🔧 Variáveis de Ambiente

### Banco de Dados (PostgreSQL)

```properties
# Tipo de banco
quarkus.datasource.db-kind=postgresql

# URL de conexão
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5444/pg-inventory

# Credenciais
quarkus.datasource.username=pg-inventory
quarkus.datasource.password=0706

# Estratégia de DDL (validate, update, create, drop-and-create)
quarkus.hibernate-orm.database.generation=validate

# Logs SQL
quarkus.hibernate-orm.log.sql=true
```

### Flyway

```properties
# Executar migrations ao iniciar
quarkus.flyway.migrate-at-start=true
```

### Kafka

```properties
# Bootstrap servers
kafka.bootstrap.servers=localhost:9092

# Consumer — Eventos de pedidos
mp.messaging.incoming.orders-in.connector=smallrye-kafka
mp.messaging.incoming.orders-in.topic=orders.v1.events
mp.messaging.incoming.orders-in.group.id=inventory
mp.messaging.incoming.orders-in.auto.offset.reset=earliest
mp.messaging.incoming.orders-in.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.orders-in.failure-strategy=dead-letter-queue
mp.messaging.incoming.orders-in.dead-letter-queue.topic=orders.v1.events.dlq

# Consumer — DLQ de pedidos
mp.messaging.incoming.orders-dlq-in.connector=smallrye-kafka
mp.messaging.incoming.orders-dlq-in.topic=orders.v1.events.dlq
mp.messaging.incoming.orders-dlq-in.group.id=inventory-dlq
mp.messaging.incoming.orders-dlq-in.auto.offset.reset=earliest
mp.messaging.incoming.orders-dlq-in.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer

# Producer — Resultado de reserva (OrderConfirmed/OrderRejected)
mp.messaging.outgoing.orders-out.connector=smallrye-kafka
mp.messaging.outgoing.orders-out.topic=orders.v1.events
mp.messaging.outgoing.orders-out.value.serializer=org.apache.kafka.common.serialization.StringSerializer

# Producer — Alerta de estoque baixo
mp.messaging.outgoing.low-stock-out.connector=smallrye-kafka
mp.messaging.outgoing.low-stock-out.topic=inventory.low-stock
mp.messaging.outgoing.low-stock-out.value.serializer=org.apache.kafka.common.serialization.StringSerializer

# Producer — DLQ de estoque baixo
mp.messaging.outgoing.low-stock-dlq-out.connector=smallrye-kafka
mp.messaging.outgoing.low-stock-dlq-out.topic=inventory.low-stock.dlq
mp.messaging.outgoing.low-stock-dlq-out.value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

---

## 🧪 Como Rodar Testes

### Pré-requisitos para Testes

- Docker (TestContainers cria ambientes isolados de PostgreSQL e Kafka)
- Java 21 JDK

### Rodar Todos os Testes

```bash
# Executar todos os testes
./mvnw test

# Com output detalhado
./mvnw test -X
```

## 🏗️ Decisões Arquiteturais

### 1. Arquitetura Hexagonal (Ports & Adapters)

**Decisão**: Implementar uma arquitetura hexagonal para desacoplar domínio de infraestrutura.

**Trade-offs**:
- ✅ **Vantagens**:
  - Fácil testes unitários (domínio sem dependências externas)
  - Migração de tecnologias sem alterar domínio (trocar BD, trocar Kafka)
  - Independência entre camadas
  - Alta coesão e baixo acoplamento

- ❌ **Desvantagens**:
  - Curva de aprendizado mais acentuada
  - Mais arquivos para mudanças simples

---

### 2. Event-Driven Architecture (Comunicação Assíncrona)

**Decisão**: Usar publicação de eventos no Kafka para comunicação entre serviços. O inventory-service consome `OrderCreated` e publica `OrderConfirmed`/`OrderRejected` e `LowStockAlert`.

**Trade-offs**:
- ✅ **Vantagens**:
  - Desacoplamento entre serviços
  - Escalabilidade horizontal
  - Auditoria de eventos
  - Permitir múltiplos subscribers

- ❌ **Desvantagens**:
  - Eventual consistency (não absoluta)
  - Complexidade de debugging
  - Necessidade de compensating transactions
  - Overhead de infraestrutura

---

### 3. Java 21 + Quarkus 3.34

**Decisão**: Usar Java 21 (LTS) com Quarkus para startup rápido e baixo consumo de memória.

**Trade-offs**:
- ✅ **Vantagens**:
  - LTS com suporte até 2029 (Java 21)
  - Startup em milissegundos (dev mode)
  - Hot reload nativo
  - Possibilidade de compilação nativa (GraalVM)

- ❌ **Desvantagens**:
  - Ecossistema menor que Spring Boot
  - Algumas bibliotecas podem não ter extensão Quarkus

---

### 6. Hibernate ORM Panache + Flyway para Persistência

**Decisão**: Usar ORM com Panache e versionamento de schema com Flyway.

**Trade-offs**:
- ✅ **Vantagens**:
  - Código de repositório simplificado (Panache)
  - Migrações versionadas e auditáveis

- ❌ **Desvantagens**:
  - Performance inferior vs SQLs nativos

---

### 7. TestContainers para Testes de Integração

**Decisão**: Usar TestContainers para provisionar PostgreSQL e Kafka durante testes.

**Trade-offs**:
- ✅ **Vantagens**:
  - Ambiente real de testes (não mocks)
  - Isolamento completo entre testes
  - Reproduzibilidade

- ❌ **Desvantagens**:
  - Testes mais lentos (~2-3x mais)
  - Requer Docker instalado
  - Overhead de resource

---

## 🔐 Melhorias Posteriores p/ Segurança

- [ ] Mudar senha PostgreSQL em produção
- [ ] Configurar autenticação Kafka (SASL)
- [ ] Colocar variáveis sensíveis em vault (HashiCorp Vault, AWS Secrets Manager)
- [ ] Habilitar autenticação nos endpoints REST
- [ ] Habilitar HTTPS
