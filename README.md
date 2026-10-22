# Raspberry Awards API

API REST para consultar os produtores com o menor e o maior intervalo entre prêmios consecutivos na categoria Pior Filme do Golden Raspberry Awards.

## Requisitos

- Java 17+
- Maven 3.8+

O H2 é embarcado e executado em memória; não é necessário instalar ou configurar um banco de dados.

## Como executar

Na pasta do projeto:

```bash
mvn spring-boot:run
```

Na inicialização, a aplicação lê `src/main/resources/Movielist.csv` e carrega os filmes no banco H2 em memória.

## Endpoint

```http
GET http://localhost:8080/api/v1/awards/intervals
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

Os testes iniciam a aplicação com o H2, verificam a carga do CSV e validam os resultados retornados pela API.

## Registro de uso de IA

Consulte [AI_INTERACTIONS.md](AI_INTERACTIONS.md) para um resumo das interações utilizadas na criação deste projeto.
