# TicketWave

Execute o comando abaixo na raiz do repositório com o Docker e o plugin Docker Compose instalados.

```sh
docker compose up
```

O Compose compila a API Spring Boot com Java 21 e inicia PostgreSQL, Redis e LocalStack. A API aguarda os três serviços e a criação do bucket S3. O Flyway aplica as migrações do banco automaticamente.

- Swagger UI em http://localhost:8080/docs
- Status da API e das conexões em http://localhost:8080/actuator/health
- PostgreSQL em `localhost:5432` e Redis em `localhost:6379`
- LocalStack em `http://localhost:4566`, com o bucket padrão `ticketwave-images`

Para iniciar em segundo plano e aguardar as verificações de saúde, use `docker compose up -d --wait`. Use `docker compose down` para parar o ambiente e preservar os volumes do PostgreSQL e do Redis. Os objetos do S3 são temporários e se perdem ao recriar o contêiner do LocalStack.

As configurações padrão funcionam sem arquivo `.env`. Para mudar as portas locais ou as credenciais de desenvolvimento, copie `.env.example` para `.env` e ajuste os valores. O usuário e o banco do PostgreSQL são definidos na primeira inicialização do volume.

Para listar os buckets locais, execute `docker compose exec localstack awslocal s3 ls`. O nome do bucket e a região podem ser ajustados em `STORAGE_BUCKET` e `STORAGE_REGION`.
