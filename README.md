# Raspberry Awards API

API REST para calcular os menores e maiores intervalos entre prêmios consecutivos na categoria Pior Filme do Golden Raspberry Awards, a partir de um CSV enviado pelo usuário.

## Requisitos

- Java 17+
- Maven 3.8+

O H2 é embarcado e executado em memória; não é necessário instalar ou configurar um banco de dados.

## Arquitetura

O projeto segue arquitetura hexagonal. O domínio e os casos de uso ficam independentes de HTTP e persistência; as portas definem as operações necessárias, e os adaptadores conectam essas portas ao Spring MVC, ao JPA/H2 e ao CSV.

```text
domain/                   Modelos e regras de domínio
application/port/in/      Casos de uso oferecidos pela aplicação
application/port/out/     Contratos para dependências externas
application/service/      Implementação dos casos de uso
adapter/in/web/           Adaptador HTTP
adapter/out/persistence/  Adaptador JPA/H2
```

## Como executar

Na pasta do projeto:

```bash
mvn spring-boot:run
```

Abra `http://localhost:8080` para acessar o Swagger UI. A API possui um único endpoint: `POST /api/v1/awards/upload`. Selecione o arquivo CSV no Swagger e execute a requisição para receber os resultados. A aplicação começa sem dados; cada arquivo válido substitui a importação anterior.

O nome original do arquivo enviado também é armazenado junto a cada filme importado.

O CSV deve usar `;` como separador e conter as colunas `year`, `title`, `studios`, `producers` e `winner`. A coluna `winner` aceita `yes`, `no` ou valor vazio. O limite de upload é 10 MB.

## Endpoint

Documentação interativa:

```text
http://localhost:8080
http://localhost:8080/swagger-ui/index.html
```

```http
POST http://localhost:8080/api/v1/awards/upload
Content-Type: multipart/form-data
```

O campo multipart deve se chamar `file`. O endpoint retorna os seguintes códigos:

| Código HTTP | Situação | Resposta |
|---|---|---|
| `200 OK` | CSV válido processado | JSON com os intervalos mínimo e máximo calculados |
| `400 Bad Request` | Arquivo vazio, ausente ou CSV inválido | JSON com `status`, `error` e `message` |
| `413 Payload Too Large` | Arquivo acima do limite de 10 MB | JSON com `status`, `error` e `message` |
| `415 Unsupported Media Type` | Arquivo sem extensão `.csv` | JSON com `status`, `error` e `message` |

Exemplo de resposta de erro:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "O CSV deve conter as colunas year, title, studios, producers e winner."
}
```

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

## Testes de integração

```bash
mvn test
```

Os testes verificam o upload de um CSV válido, os resultados calculados e a resposta para um arquivo inválido.

## Registro de uso de IA

Consulte [AI_INTERACTIONS.md](AI_INTERACTIONS.md) para um resumo das interações utilizadas na criação deste projeto.
