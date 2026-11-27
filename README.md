# TicketWave

API REST para gerenciamento e descoberta de eventos. A versão atual oferece cadastro e autenticação de usuários, além de criação, atualização, listagem e busca paginada de eventos. O projeto usa Java 21 e Spring Boot, PostgreSQL com migrações Flyway, Redis e LocalStack para emular o Amazon S3 no desenvolvimento.

O [documento do projeto](docs/TicketWave%20Documenta%C3%A7%C3%A3o%20do%20Projeto.pdf) descreve a arquitetura e os fluxos previstos para a plataforma.

## Executar localmente

Com o Docker e o plugin Docker Compose instalados, execute na raiz do repositório. Na primeira configuração, crie o arquivo local com `cp .env.exemple .env` e preencha os campos vazios antes de iniciar os serviços.

- Gere `POSTGRES_PASSWORD` com `openssl rand -hex 32`.
- Gere `JWT_SECRET` com `openssl rand -base64 64 | tr -d '\n'`.
- Use `test` em `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY` para o LocalStack.

O Git ignora `.env`. Se já houver um volume PostgreSQL, use a senha existente no arquivo ou altere a senha no banco antes de atualizar o arquivo. Ao trocar a chave JWT, os tokens de acesso emitidos com a chave antiga deixam de ser válidos.

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

Ajuste as portas, o nome do bucket e a região em `.env`. Os modelos [`.env.exemple`](.env.exemple) e [`.env.example`](.env.example) têm os mesmos campos.

```sh
docker compose logs -f app
docker compose exec localstack awslocal s3 ls
docker compose down
```

`docker compose down` preserva os volumes do PostgreSQL e do Redis. Os objetos do S3 local são temporários e se perdem quando o contêiner do LocalStack é recriado.

## Usar a API

1. No Swagger, use `POST /v1/api/auth/register` para cadastrar um usuário. Informe nome, e-mail, telefone, data de nascimento (`birthDate`, no formato `AAAA-MM-DD`), CPF (`cpf`), senha e confirmação (`passwordConfirmation`). O CPF aceita 11 dígitos ou o formato com pontos e hífen. Envie o endereço em `address` com `zipCode`, `street`, `number`, `district`, `city`, `state` e, opcionalmente, `complement`. No formulário Angular, **Buscar CEP** consulta o ViaCEP e preenche os campos disponíveis. Preencha o número e confira o endereço antes de enviar.
2. Use `POST /v1/api/auth/login` para obter `accessToken` e `refreshToken`. Informe o token de acesso no botão **Authorize** do Swagger ou envie `Authorization: Bearer <accessToken>` nas rotas protegidas.
3. Consulte `GET /v1/api/event` para listar eventos ou `GET /v1/api/event/search` para buscar com filtros. A criação, atualização e exclusão exigem `ROLE_ORGANIZER`. O organizador só pode alterar e excluir os próprios eventos. O cadastro público atribui `ROLE_CUSTOMER` (participante).

### Login com Google

Crie um cliente OAuth do tipo **Aplicativo da Web** no Google Cloud e inclua a origem do frontend em **Origens JavaScript autorizadas**. Para desenvolvimento, use `http://localhost:4200`. Defina o ID em `GOOGLE_CLIENT_ID` no `.env` e reinicie a API. O botão é exibido nas telas de login e cadastro quando o ID estiver configurado.

No primeiro acesso com um e-mail Gmail ou Google Workspace verificado, a TicketWave cria uma conta de participante ou vincula a conta existente. Contas com privilégios ou e-mail de terceiros exigem a senha da TicketWave para o primeiro vínculo. Para um endereço de terceiros sem cadastro, crie a conta na TicketWave antes de vincular o Google. Os próximos acessos usam o identificador estável da conta Google.

### Recuperação de senha

Na tela de login, selecione **Esqueci minha senha**. No desenvolvimento com Compose, abra a [caixa de entrada local do Mailpit](http://localhost:8025) para acessar o link. O link expira em 30 minutos e pode ser usado uma vez. Mantenha o frontend rodando na URL configurada em `FRONTEND_URL`.

Para usar um servidor SMTP externo, configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_FROM` e, conforme seu provedor, `SMTP_USER`, `SMTP_PASSWORD`, `SMTP_AUTH` e `SMTP_STARTTLS` no ambiente. Use a URL HTTPS do frontend no ambiente publicado.

### Perfil do participante

Em **Minha conta**, participantes podem criar, visualizar, editar e excluir seu próprio perfil.
A API disponibiliza `GET`, `PUT` e `DELETE` em `/v1/api/profile/me`.
Envie os campos `name`, `occupation`, `city`, `bio` e a foto opcional `photo` via `multipart/form-data` no `PUT`.
Use `removePhoto=true` para remover uma foto existente. A exclusão apaga o perfil e preserva a conta.
O perfil aceita até dez tags em `interests` e objetivos em `goals`, enviados como campos repetidos.
`GET /v1/api/profile/me/suggestions` sugere participantes ativos por interesses iguais e objetivos compatíveis, como contratar e ser contratado.

### Usar a home Angular

Com a API rodando na porta 8080, execute a partir da raiz do projeto.

```sh
cd app/frontend/ticket-fron
npm ci
npm start
```

Abra `http://localhost:4200` e selecione **Entrar**. Use **Criar uma conta** para fazer o cadastro. Após o login, **Minha conta** mostra o perfil e a opção de sair. O frontend renova a sessão automaticamente enquanto a página está aberta. Os tokens ficam em memória, por isso recarregar a página exige um novo login.

O servidor de desenvolvimento encaminha `/v1/api/**` para a API pelo arquivo `proxy.conf.json`. Se mudar a porta publicada da API, ajuste o destino nesse arquivo.

Depois de atualizar o código do backend, use `docker compose up -d --build app` na raiz do projeto para que o proxy continue apontando para a versão atual da API.

As rotas protegidas respondem com HTTP 401 quando não há um token de acesso válido. Envie `{"refreshToken":"..."}` para `POST /v1/api/auth/refresh` e substitua os dois tokens pelos valores retornados. Cada renovação invalida o refresh token enviado. Um novo login substitui a sessão de renovação anterior da conta.

Para revogar a renovação, envie o refresh token atual para `POST /v1/api/auth/logout`. O JWT de acesso já emitido segue válido até expirar. O campo `expiresIn` é dado em milissegundos.

### Erros da API

As respostas de erro usam JSON. O campo `status` corresponde ao status HTTP, `path` identifica o caminho da requisição e `violations` contém os erros de validação por campo. A lista fica vazia quando não há erros de campo.

```json
{
  "timestamp": "2026-11-23T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados de entrada inválidos.",
  "path": "/v1/api/auth/login",
  "violations": [
    { "field": "email", "message": "deve ser um endereço de e-mail bem formado" }
  ]
}
```

## Estrutura do repositório

| Caminho | Conteúdo |
| --- | --- |
| [`app/`](app/) | Aplicação Spring Boot e testes de integração |
| [`app/backend/src/main/resources/db/migration/`](app/backend/src/main/resources/db/migration/) | Migrações SQL versionadas pelo Flyway |
| [`compose.yaml`](compose.yaml) | Ambiente local da API e dos serviços |
| [`infra/`](infra/) | Inicialização do bucket S3 no LocalStack |
| [`.github/workflows/ci.yml`](.github/workflows/ci.yml) | Workflow de build e testes |

O código Java está organizado em [`modules`](app/backend/src/main/java/com/projeto/ticket_wave/modules) e [`shared`](app/backend/src/main/java/com/projeto/ticket_wave/shared). Os módulos são `identity` para usuários e autenticação, `events` para eventos e catálogo, `ticketing` para lotes, pedidos e ingressos, e `networking` para conexões.

Dentro de cada módulo, `domain` contém os modelos, `application` reúne os casos de uso e suas interfaces, `adapters/rest` recebe as chamadas HTTP e `infrastructure` implementa integrações como a persistência JPA. Componentes reutilizados entre módulos ficam em `shared`.

## Build e testes

Com Java 21 e o Docker em execução, rode o Maven Wrapper na raiz do repositório.

```sh
./mvnw -B -ntp -f app/backend/pom.xml verify
```

O Testcontainers provisiona PostgreSQL e Redis para os testes de integração. O GitHub Actions executa o mesmo comando a cada pull request, e a checagem **Build e testes** precisa passar para liberar o merge na `main`.
