# TicketWave

API REST para gerenciamento e descoberta de eventos. A versão atual oferece cadastro e autenticação de usuários, além de criação, atualização, listagem e busca paginada de eventos. O projeto usa Java 21 e Spring Boot, PostgreSQL com migrações Flyway, Redis e LocalStack para emular o Amazon S3 no desenvolvimento.

O [documento do projeto](docs/TicketWave%20Documenta%C3%A7%C3%A3o%20do%20Projeto.pdf) descreve a arquitetura e os fluxos previstos para a plataforma.

## Executar localmente

Com o Docker e o plugin Docker Compose instalados, execute na raiz do repositório.

```sh
docker compose up -d --wait
```

O Compose compila a API, inicia PostgreSQL, Redis e LocalStack e aguarda a criação do bucket `ticketwave-images`. O Flyway aplica as migrações ao iniciar a API.

| Serviço | Endereço padrão |
| --- | --- |
| Swagger UI | [localhost:8080/docs](http://localhost:8080/docs) |
| OpenAPI | [localhost:8080/api-docs](http://localhost:8080/api-docs) |
| Saúde da API | [localhost:8080/actuator/health](http://localhost:8080/actuator/health) |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |
| LocalStack | `localhost:4566` |

Para personalizar portas, credenciais locais, nome do bucket ou região, copie [`.env.example`](.env.example) para `.env` e ajuste os valores. O PostgreSQL usa as credenciais configuradas na primeira inicialização do volume.

```sh
docker compose logs -f app
docker compose exec localstack awslocal s3 ls
docker compose down
```

`docker compose down` preserva os volumes do PostgreSQL e do Redis. Os objetos do S3 local são temporários e se perdem quando o contêiner do LocalStack é recriado.

## Usar a API

1. No Swagger, use `POST /v1/api/auth/register` para cadastrar um usuário.
2. Use `POST /v1/api/auth/login` para obter o token de acesso e informe-o no botão **Authorize** do Swagger.
3. Consulte `GET /v1/api/event` para listar eventos ou `GET /v1/api/event/search` para buscar com filtros. A criação e a atualização exigem o papel de administrador ou organizador.

## Estrutura do repositório

| Caminho | Conteúdo |
| --- | --- |
| [`app/`](app/) | Aplicação Spring Boot e testes de integração |
| [`app/src/main/resources/db/migration/`](app/src/main/resources/db/migration/) | Migrações SQL versionadas pelo Flyway |
| [`compose.yaml`](compose.yaml) | Ambiente local da API e dos serviços |
| [`infra/`](infra/) | Inicialização do bucket S3 no LocalStack |
| [`.github/workflows/ci.yml`](.github/workflows/ci.yml) | Workflow de build e testes |

O código Java está organizado em [`modules`](app/src/main/java/com/projeto/ticket_wave/modules) e [`shared`](app/src/main/java/com/projeto/ticket_wave/shared). Os módulos são `identity` para usuários e autenticação, `events` para eventos e catálogo, `ticketing` para lotes, pedidos e ingressos, e `networking` para conexões.

Dentro de cada módulo, `domain` contém os modelos, `application` reúne os casos de uso e suas interfaces, `adapters/rest` recebe as chamadas HTTP e `infrastructure` implementa integrações como a persistência JPA. Componentes reutilizados entre módulos ficam em `shared`.

## Build e testes

Com Java 21 e o Docker em execução, rode o Maven Wrapper na raiz do repositório.

```sh
./mvnw -B -ntp -f app/pom.xml verify
```

O Testcontainers provisiona PostgreSQL e Redis para os testes de integração. O GitHub Actions executa o mesmo comando a cada pull request, e a checagem **Build e testes** precisa passar para liberar o merge na `main`.
