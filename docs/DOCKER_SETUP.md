# TicketWave - Docker Compose Configuration

Configuração robusta e production-ready do Docker Compose para o projeto TicketWave.

## 📋 Serviços Incluídos

### Serviços de Infraestrutura

1. **PostgreSQL 17** (Banco de dados principal)
   - Porta: `5432`
   - Health checks automáticos
   - Volumes persistentes
   - Pool de conexões configurável
   - Inicialização automática com extensões (UUID, pg_trgm, unaccent)

2. **Redis 7** (Cache e sessões)
   - Porta: `6379`
   - Persistência em disco (RDB + AOF)
   - Limite de memória com política LRU
   - Health checks automáticos

3. **RabbitMQ 3.13** (Message broker)
   - Porta AMQP: `5672`
   - Porta Management: `15672`
   - Configuração robusta em `rabbitmq.conf`
   - Health checks automáticos
   - Dashboard de gerenciamento acessível

4. **LocalStack 3.8** (AWS Services Mock)
   - Porta: `4566`
   - Serviços: S3, SQS, SNS, DynamoDB, Lambda, etc.
   - Inicialização automática de recursos via script
   - Ideal para desenvolvimento e testes

5. **TicketWave Application** (Aplicação Spring Boot)
   - Porta: `8080`
   - Construção multi-stage otimizada
   - Espera por todas as dependências
   - Health checks via actuator

## 🚀 Quick Start

### Pré-requisitos

- Docker 20.10+
- Docker Compose 2.0+
- Git

### Instalação e Execução

1. **Clone o repositório**
   ```bash
   git clone <repository-url>
   cd TicketWave
   ```

2. **Configure as variáveis de ambiente**
   ```bash
   cp .env.example .env
   # Editar .env conforme necessário (valores padrão funcionam para desenvolvimento)
   ```

3. **Inicie os serviços**
   ```bash
   # Usar o script helper (recomendado)
   ./docker.sh up
   
   # Ou usar docker-compose diretamente
   cd infra
   docker-compose up -d
   ```

4. **Verifique o status dos serviços**
   ```bash
   ./docker.sh ps
   ```

5. **Veja os logs**
   ```bash
   ./docker.sh logs
   ```

## 📝 Configuração via .env

O arquivo `.env` controla todos os aspectos da infraestrutura:

```env
# Application
APP_NAME=ticket-wave
APP_ENV=docker
APP_PORT=8080

# PostgreSQL
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=ticketwave

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# AWS/LocalStack
AWS_ENDPOINT_URL=http://localstack:4566
AWS_REGION=us-east-1
```

## 🛠️ Comandos Disponíveis

### Usando o script helper

```bash
# Iniciar todos os serviços
./docker.sh up

# Parar todos os serviços
./docker.sh down

# Ver status dos serviços
./docker.sh ps

# Ver logs em tempo real
./docker.sh logs

# Verificar saúde de todos os serviços
./docker.sh healthcheck

# Reiniciar todos os serviços
./docker.sh restart

# Remover containers e volumes (limpeza completa)
./docker.sh clean

# Reconstruir imagens Docker
./docker.sh build

# Ver ajuda
./docker.sh help
```

### Usando docker-compose diretamente

```bash
cd infra

# Iniciar serviços
docker-compose up -d

# Parar serviços
docker-compose down

# Ver status
docker-compose ps

# Ver logs
docker-compose logs -f

# Executar comando em um container
docker-compose exec postgres psql -U postgres -d ticketwave

# Reconstruir
docker-compose build

# Remover tudo
docker-compose down -v
```

## 🏥 Health Checks

Todos os serviços possuem health checks automáticos:

- **PostgreSQL**: Verifica `pg_isready` a cada 10s (5 tentativas)
- **Redis**: Verifica `PING` a cada 10s (5 tentativas)
- **RabbitMQ**: Verifica diagnostics a cada 10s (5 tentativas)
- **LocalStack**: Verifica listagem de S3 a cada 15s (5 tentativas)
- **Aplicação**: Verifica `/actuator/health` a cada 10s (5 tentativas)

Todos os serviços aguardam a saúde dos dependentes antes de iniciar.

## 💾 Volumes e Persistência

Volumes Docker gerenciados:

- `postgres_data` - Dados do PostgreSQL
- `redis_data` - Dados do Redis (RDB + AOF)
- `rabbitmq_data` - Dados do RabbitMQ
- `localstack_data` - Dados do LocalStack

Os dados persistem entre reinicializações. Para limpar tudo:

```bash
./docker.sh clean
```

## 🌐 Redes

Os serviços rodam em uma rede Docker isolada (`ticketwave-network`) com IP fixo:

- PostgreSQL: `172.20.0.2:5432`
- Redis: `172.20.0.3:6379`
- RabbitMQ: `172.20.0.4:5672`
- LocalStack: `172.20.0.5:4566`
- Aplicação: `172.20.0.10:8080`

Acesso do host (localhost):
- App: http://localhost:8080
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- RabbitMQ: localhost:5672 (AMQP) e http://localhost:15672 (Management)
- LocalStack: localhost:4566

## 📊 Recursos (Limits)

Cada container possui limites de recursos definidos:

| Serviço | Memória | CPU |
|---------|---------|-----|
| PostgreSQL | 512 MB | 1.0 |
| Redis | 256 MB | 0.5 |
| RabbitMQ | 512 MB | 1.0 |
| LocalStack | 1 GB | 1.5 |
| Aplicação | 1 GB | 2.0 |

Ajuste conforme necessário em `docker-compose.yml`.

## 📚 Inicialização Automática

### PostgreSQL (`init-db.sql`)
- Cria extensões necessárias (UUID, pg_trgm, unaccent)
- Cria schema ticketwave
- Configura timezone e permissões

### LocalStack (`localstack-init.sh`)
- Cria bucket S3 `ticketwave-bucket`
- Cria fila SQS `ticketwave-queue`
- Cria tópico SNS `ticketwave-notifications`
- Cria tabela DynamoDB `ticketwave-sessions`

### RabbitMQ (`rabbitmq.conf`)
- Configurações de performance
- Limites de memória
- Políticas de clustering

## 🔍 Troubleshooting

### Serviço não inicia
```bash
# Verificar logs
./docker.sh logs

# Ou para um serviço específico
cd infra
docker-compose logs postgres
```

### Port já em uso
```bash
# Modificar portas em .env ou docker-compose.yml
# Exemplo: POSTGRES_PORT=5433
```

### Remover tudo e recomeçar
```bash
./docker.sh clean
./docker.sh build
./docker.sh up
```

### Conectar ao PostgreSQL
```bash
cd infra
docker-compose exec postgres psql -U postgres -d ticketwave
```

### Acessar RabbitMQ Management
Abra: http://localhost:15672
- User: `guest`
- Password: `guest`

### Verificar arquivos do LocalStack
```bash
cd infra
docker-compose exec localstack bash
ls -la /var/lib/localstack
```

## 🔐 Segurança (Desenvolvimento)

⚠️ **IMPORTANTE**: Esta configuração é otimizada para **desenvolvimento**. Para produção:

1. Altere todas as senhas padrão em `.env`
2. Use volumes remotos para dados persistentes
3. Configure SSL/TLS
4. Implemente autenticação apropriada
5. Use secrets manager do Docker
6. Configure firewalls e network policies
7. Configure backups automáticos
8. Monitore logs e métricas

## 📝 Variáveis de Ambiente

Veja `.env.example` para todas as variáveis disponíveis com descrições.

Principais categorias:
- Application
- PostgreSQL
- Redis
- RabbitMQ
- AWS/LocalStack
- Logging
- Spring

## 🎯 Performance

Configurações de performance aplicadas:

- **PostgreSQL**: Connection pooling (Hikari), índices
- **Redis**: Política LRU, persistência assíncrona
- **RabbitMQ**: Channel limits, heartbeat configurado
- **LocalStack**: Executor de Lambda otimizado
- **Aplicação**: Java heap otimizado

## 📖 Próximos Passos

1. Configure o `.env` conforme necessário
2. Execute `./docker.sh up`
3. Acesse a aplicação em http://localhost:8080
4. Verifique logs com `./docker.sh logs`
5. Consulte documentação do Spring Boot para configurações específicas

## 🆘 Suporte

Para problemas, verifique:
1. Logs: `./docker.sh logs`
2. Health checks: `./docker.sh healthcheck`
3. Docker status: `docker ps`
4. Espaço em disco: `df -h`
5. Memória disponível: `free -h`

---

**Última atualização**: 2026-06-29
**Versão**: 1.0
