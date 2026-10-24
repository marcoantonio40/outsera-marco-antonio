# Registro de interações com IA

Projeto criado com assistência de IA, conforme solicitado pela avaliação.

## Interações

- A IA analisou os requisitos e implementou uma API REST com Spring Boot, importação do CSV para H2 em memória, cálculo dos intervalos entre prêmios e teste de integração.
- O usuário solicitou a adoção de arquitetura hexagonal; a IA separou domínio, caso de uso, portas e adaptadores HTTP, JPA e CSV, preservando o endpoint existente.
- O usuário solicitou upload de CSV pelo navegador; a IA adicionou uma página de envio, validação do arquivo, importação no H2 e respostas HTTP/JSON para erros.
- O usuário solicitou Swagger na URL inicial; a IA adicionou Swagger UI, redirecionamento da raiz e documentação dos endpoints.
- O usuário esclareceu que deve existir apenas um endpoint; a IA removeu a consulta separada e manteve somente o upload que retorna os resultados.
- O usuário solicitou que o nome do arquivo enviado fosse armazenado no domínio e na entidade de persistência.
- O usuário solicitou logs com códigos e mensagens em todas as classes do fluxo de upload; a IA adicionou logs ao controller, parser, serviços, persistência e tratamento de erros.
- O usuário solicitou a padronização dos logCodes no formato `SIGLA_DA_CLASSE-SIGLA_DO_MÉTODO-SEQUÊNCIA`, com siglas de pelo menos dois caracteres; a IA atualizou os códigos nas classes do fluxo, por exemplo `MCP-PAR-00` e `CPIS-CAL-00`.
- O usuário solicitou acesso ao banco de dados; a IA habilitou o console web do H2 em `/h2-console`, documentou os dados de conexão no README e manteve o banco em memória, alertando que os dados são perdidos ao encerrar a aplicação.
