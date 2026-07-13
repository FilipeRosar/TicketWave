# ============================================================================
# Development Setup Guide - Docker Compose
# ============================================================================
# Este arquivo contém instruções passo a passo para configurar o ambiente
# de desenvolvimento com Docker Compose.
# ============================================================================

## 🎯 Objetivos

- [x] Configurar .env robustamente
- [x] Criar docker-compose.yml com todas as melhores práticas
- [x] Implementar health checks em todos os serviços
- [x] Adicionar RabbitMQ (estava faltando)
- [x] Configurar volumes e redes appropriadamente
- [x] Criar scripts de inicialização automática
- [x] Adicionar documentação completa

## 📦 Arquivos Criados/Modificados

### Configuração Principal
- `.env` - Variáveis de ambiente (atualizado com valores corretos)
- `.env.example` - Exemplo de configuração (novo)

### Docker Compose
- `infra/docker-compose.yml` - Configuração completa com robustez (atualizado)

### Scripts de Inicialização
- `infra/init-db.sql` - Inicialização do PostgreSQL (novo)
- `infra/rabbitmq.conf` - Configuração do RabbitMQ (novo)
- `infra/localstack-init.sh` - Inicialização do LocalStack (novo)

### Helper Scripts
- `docker.sh` - Script bash para gerenciar containers (novo)
- `app/.dockerignore` - Otimizações de build (novo)

### Documentação
- `docs/DOCKER_SETUP.md` - Guia completo de uso (novo)

## 🚀 Como Usar

### 1. Verificar Configuração
```bash
cat .env
./docker.sh ps
```

### 2. Iniciar Ambiente
```bash
./docker.sh up
```

### 3. Verificar Saúde
```bash
./docker.sh healthcheck
```

### 4. Ver Logs
```bash
./docker.sh logs
```

### 5. Parar Ambiente
```bash
./docker.sh down
```

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                 Docker Network (172.20.0.0/16)         │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌───────────────┐  ┌──────────────┐  ┌─────────────┐  │
│  │   PostgreSQL  │  │    Redis     │  │  RabbitMQ   │  │
│  │   (172.20.0.2)│  │ (172.20.0.3) │  │(172.20.0.4) │  │
│  │    5432       │  │    6379      │  │   5672/    │  │
│  └───────────────┘  └──────────────┘  │   15672     │  │
│                                         └─────────────┘  │
│                                                           │
│  ┌────────────────────┐            ┌──────────────────┐ │
│  │    LocalStack      │            │  TicketWave App  │ │
│  │   (172.20.0.5)     │            │ (172.20.0.10)    │ │
│  │      4566          │◄──────────►│     8080         │ │
│  │  (S3, SQS, etc)    │            │  Spring Boot     │ │
│  └────────────────────┘            └──────────────────┘ │
│                                                           │
└─────────────────────────────────────────────────────────┘
           ▲                                      ▲
           │ localhost:5432                       │
           │ localhost:6379                       │ localhost:8080
           │ localhost:5672                       │
           │ localhost:15672                      │
           │ localhost:4566                       │
           │                                      ▼
        ┌──────────────────────────┐
        │      Host Machine        │
        │      (Developer)         │
        └──────────────────────────┘
```

## 🎯 Recursos Configurados

### Health Checks
✅ PostgreSQL - pg_isready
✅ Redis - PING
✅ RabbitMQ - diagnostics
✅ LocalStack - S3 ls
✅ Aplicação - /actuator/health

### Resource Limits
✅ PostgreSQL - 512MB / 1 CPU
✅ Redis - 256MB / 0.5 CPU
✅ RabbitMQ - 512MB / 1 CPU
✅ LocalStack - 1GB / 1.5 CPU
✅ Aplicação - 1GB / 2 CPU

### Volumes Persistentes
✅ postgres_data - Banco de dados
✅ redis_data - Cache e sessões
✅ rabbitmq_data - Mensagens
✅ localstack_data - Dados AWS

### Networks
✅ ticketwave-network (Bridge)
✅ IPs fixos para cada serviço
✅ Comunicação inter-container

### Inicialização Automática
✅ PostgreSQL - Extensões e schema
✅ LocalStack - S3, SQS, SNS, DynamoDB
✅ RabbitMQ - Configuração robusta

## 📊 Checklist de Verificação

Após executar `./docker.sh up`:

- [ ] Docker Desktop está rodando
- [ ] .env existe com configurações corretas
- [ ] `docker-compose ps` mostra todos os serviços UP
- [ ] `./docker.sh healthcheck` mostra todos saudáveis
- [ ] Acesso PostgreSQL: `docker-compose exec postgres psql -U postgres`
- [ ] Acesso Redis: `docker-compose exec redis redis-cli`
- [ ] Acesso RabbitMQ: http://localhost:15672 (guest/guest)
- [ ] Acesso LocalStack: `docker-compose exec localstack awslocal s3 ls`
- [ ] Acesso App: http://localhost:8080

## 🔧 Troubleshooting Rápido

| Problema | Solução |
|----------|---------|
| Port já em uso | Altere em `.env` ou `docker-compose.yml` |
| Container não inicia | `./docker.sh logs` para ver erro |
| Memória insuficiente | Reduza limites em `docker-compose.yml` |
| Volumes cheios | `./docker.sh clean` para remover dados |
| Conexão recusada | Aguarde health checks (30s) |
| Erro de permissão | `chmod +x docker.sh` |

## 📝 Próximos Passos

1. ✅ Executar `./docker.sh up`
2. ✅ Verificar status com `./docker.sh ps`
3. ✅ Executar `./docker.sh healthcheck`
4. ✅ Consultar `docs/DOCKER_SETUP.md` para mais detalhes
5. ✅ Iniciar desenvolvimento com a aplicação

## 🔐 Segurança - Lembrete para Produção

Para produção, você deve:
- Alterar todas as senhas padrão
- Usar secrets manager
- Configurar SSL/TLS
- Implementar backups
- Monitorar logs e métricas
- Usar volumes remotos
- Configurar autoscaling

## 📚 Referências

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Redis Docker Image](https://hub.docker.com/_/redis)
- [RabbitMQ Docker Image](https://hub.docker.com/_/rabbitmq)
- [LocalStack Documentation](https://docs.localstack.cloud/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)

---

**Status**: ✅ Configuração Completa e Robusta
**Última Atualização**: 2026-06-29
