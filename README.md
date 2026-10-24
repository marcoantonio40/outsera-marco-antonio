# Raspberry Awards API

API REST para importar uma lista de filmes em CSV e calcular os menores e maiores intervalos entre vitórias consecutivas de produtores na categoria Pior Filme do Golden Raspberry Awards.

## Requisitos

- Java 17 ou superior
- Maven 3.8 ou superior

O H2 é embarcado e executado em memória; não é necessário instalar ou configurar um banco de dados.

## Arquitetura

O projeto segue arquitetura hexagonal: os casos de uso coordenam o fluxo, as portas definem as operações necessárias e os adaptadores conectam a aplicação ao HTTP, ao CSV e à persistência.

```text
application/
  port/in/                 Casos de uso oferecidos pela aplicação
  port/out/                Contratos para dependências externas
  service/                 Implementações dos casos de uso
adapter/
  in/web/                  Controller, parser CSV e tratamento de erros
    request/               Dados recebidos pela API (*Request)
    response/              Respostas HTTP (*Response)
  out/persistence/         Adaptador e repositório JPA
    entity/                Entidades persistidas (*Entity)
domain/
  dto/                     Dados compartilhados entre as classes (*DTO)
```

O upload é recebido em `MovieUploadRequest`, as respostas usam classes `*Response`, os dados que circulam entre as camadas são representados por DTOs e `MovieEntity` representa os filmes persistidos.

## Como executar

Na raiz do projeto, inicie a aplicação:

```bash
mvn spring-boot:run
```

Acesse `http://localhost:8080` para abrir o Swagger UI. A aplicação começa sem filmes importados. Cada CSV válido substitui o conjunto de filmes da importação anterior, e o nome original do arquivo é armazenado junto a cada filme.

## CSV aceito

O arquivo deve ter extensão `.csv` (a verificação não diferencia maiúsculas de minúsculas), usar `;` como separador e conter os cabeçalhos `year`, `title`, `studios`, `producers` e `winner`. Os nomes dos cabeçalhos também não diferenciam maiúsculas de minúsculas. Linhas vazias são ignoradas.

```csv
year;title;studios;producers;winner
1990;Filme de exemplo;Estúdio de exemplo;Produtor de exemplo;yes
1991;Outro filme;Outro estúdio;Produtor de exemplo;
```

Cada linha precisa conter um ano numérico positivo e valores não vazios para `title`, `studios` e `producers`. Em `winner`, somente `yes` (em qualquer combinação de maiúsculas e minúsculas) é considerado vencedor; qualquer outro valor, inclusive `no` ou vazio, é considerado não vencedor. O CSV deve conter pelo menos um registro. O tamanho máximo do arquivo é 10 MB.

Os produtores listados em uma linha são separados por vírgula e/ou pela palavra `and`. Para cada produtor, a API ordena os anos de vitória e calcula intervalos entre vitórias consecutivas.

## Endpoint

Documentação interativa:

```text
http://localhost:8080
http://localhost:8080/swagger-ui/index.html
```

Envie o arquivo no campo multipart chamado `file`:

```http
POST http://localhost:8080/api/v1/awards/upload
Content-Type: multipart/form-data
```

| Código HTTP | Situação | Resposta |
|---|---|---|
| `200 OK` | CSV válido processado | JSON com os intervalos mínimo e máximo |
| `400 Bad Request` | Arquivo ausente/vazio ou CSV inválido | JSON com `status`, `error` e `message` |
| `413 Payload Too Large` | Arquivo acima do limite de 10 MB | JSON com `status`, `error` e `message` |
| `415 Unsupported Media Type` | Arquivo sem extensão `.csv` | JSON com `status`, `error` e `message` |

Exemplo de resposta:

```json
{
  "min": [
    {
      "producer": "Joel Silver",
      "interval": 1,
      "previousWin": 1990,
      "followingWin": 1991
    }
  ],
  "max": [
    {
      "producer": "Matthew Vaughn",
      "interval": 13,
      "previousWin": 2002,
      "followingWin": 2015
    }
  ]
}
```

Se não houver intervalos entre vitórias consecutivas, `min` e `max` são listas vazias.

Exemplo de resposta de erro:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "O CSV deve conter as colunas year, title, studios, producers e winner."
}
```

## Acesso ao banco de dados

O H2 usa a URL `jdbc:h2:mem:raspberryawards;DB_CLOSE_DELAY=-1`. Para habilitar o console durante o desenvolvimento, adicione ao `src/main/resources/application.properties`:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Reinicie a aplicação e acesse `http://localhost:8080/h2-console`. Use os seguintes dados:

- **JDBC URL:** `jdbc:h2:mem:raspberryawards;DB_CLOSE_DELAY=-1`
- **Usuário:** `sa`
- **Senha:** deixe em branco

Por exemplo, execute `SELECT * FROM MOVIES;` para consultar os filmes importados. Os dados ficam apenas na memória e são perdidos quando a aplicação é encerrada. Habilite o console somente em desenvolvimento e não o exponha em produção sem proteção.

## Testes

Execute a suíte de testes com:

```bash
mvn test
```

Os testes de integração cobrem o endpoint e a documentação OpenAPI, o processamento de CSV válido, a comparação de cabeçalhos sem diferenciar maiúsculas de minúsculas, o tratamento da coluna `winner` e as respostas para uploads inválidos.

## Registro de uso de IA

Consulte [AI_INTERACTIONS.md](AI_INTERACTIONS.md) para um resumo das interações utilizadas na criação e evolução deste projeto.
