# Delivery System

Projeto Diamante 02 - Java Advanced

## Integrantes

- Vitória Valentina - RM 563509
- Marina Magalhães - RM 561786
- João Pedro  - RM 564339


# Roteiro de Testes — Delivery System

Todos os comandos devem ser executados no PowerShell, na raiz do projeto `delivery-system`. Utilize `curl.exe` (com `.exe`) para fazer as requisições HTTP.

## 1. O que precisa estar rodando

Inicie cada comando em um terminal separado, na seguinte ordem.

### 1.1. Subir o Docker Compose

```powershell
docker compose up -d
```

### 1.2. Iniciar o Eureka Server

```powershell
./gradlew :eureka-server:bootRun
```

### 1.3. Iniciar a primeira instância do Payment Service

```powershell
./gradlew :payment-service:bootRun
```

### 1.4. Iniciar a segunda instância do Payment Service

```powershell
./gradlew :payment-service:bootRun --args='--server.port=8082'
```

### 1.5. Iniciar o Order Service

```powershell
./gradlew :order-service:bootRun
```

### 1.6. Iniciar o Review Service

```powershell
./gradlew :review-service:bootRun
```

### 1.7. Configurar o Ollama para o endpoint `/assistant`

O Ollama precisa estar rodando, com o modelo `llama3.2` baixado.

Baixe o modelo, caso ainda não esteja instalado:

```powershell
ollama pull llama3.2
```

No arquivo `order-service/src/main/resources/application.properties`, configure:

```properties
spring.ai.openai.base-url=http://localhost:11434/v1
spring.ai.openai.api-key=${OPENAI_API_KEY:ollama}
spring.ai.openai.chat.options.model=llama3.2
```

Aguarde aproximadamente 30 segundos após iniciar todos os serviços para que eles se registrem e se descubram pelo Eureka.

## 2. Criar os arquivos temporários de teste

Execute os comandos abaixo na raiz do projeto:

```powershell
'{"dishId":2,"quantity":1}' | Out-File -Encoding ascii order.json
'{"dishId":2,"quantity":0}' | Out-File -Encoding ascii order-bad.json
'{"dishId":999,"quantity":1}' | Out-File -Encoding ascii order-404.json

'{"dishId":1,"rating":5,"comment":"Great"}' | Out-File -Encoding ascii review.json
'{"dishId":1,"rating":6,"comment":"Bad"}' | Out-File -Encoding ascii review-bad.json
'{"dishId":999,"rating":5,"comment":"x"}' | Out-File -Encoding ascii review-404.json

'{"question":"Tem prato vegetariano ate R$ 40?"}' | Out-File -Encoding utf8 q.json
'{"question":"Quem ganhou a Copa de 2002?"}' | Out-File -Encoding utf8 q2.json
```

## 3. Testes e resultados esperados

### 3.1. Consulta de pratos

**Teste 1 — Listar todos os pratos**

```powershell
curl.exe -i http://localhost:8080/dishes
```

**Esperado:** HTTP `200 OK`, com uma lista de pratos contendo `id`, `name`, `description`, `price` e `stock`.

**Teste 2 — Consultar um prato existente**

```powershell
curl.exe -i http://localhost:8080/dishes/1
```

**Esperado:** HTTP `200 OK`, com os dados de um prato.

**Teste 3 — Consultar um prato inexistente**

```powershell
curl.exe -i http://localhost:8080/dishes/999
```

**Esperado:** HTTP `404 Not Found`.

```json
{"error":"Dish not found"}
```

### 3.2. Criação e consulta de pedidos

**Teste 4 — Criar um pedido válido**

```powershell
curl.exe -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" --data "@order.json"
```

**Esperado:** HTTP `201 Created`, com `id`, `dishId`, `quantity`, `totalPrice`, `status: "CONFIRMED"` e `createdAt`.

**Teste 5 — Criar um pedido com quantidade inválida**

```powershell
curl.exe -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" --data "@order-bad.json"
```

**Esperado:** HTTP `400 Bad Request`.

```json
{"error":"Quantity must be at least 1"}
```

**Teste 6 — Criar um pedido para um prato inexistente**

```powershell
curl.exe -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" --data "@order-404.json"
```

**Esperado:** HTTP `404 Not Found`.

```json
{"error":"Dish not found"}
```

**Teste 7 — Consultar um pedido existente**

```powershell
curl.exe -i http://localhost:8080/orders/1
```

**Esperado:** HTTP `200 OK`, com os dados do pedido.

**Teste 8 — Consultar um pedido inexistente**

```powershell
curl.exe -i http://localhost:8080/orders/999
```

**Esperado:** HTTP `404 Not Found`.

```json
{"error":"Order not found"}
```

### 3.3. Criação de avaliações e ranking

**Teste 9 — Criar uma avaliação válida**

```powershell
curl.exe -i -X POST http://localhost:8080/reviews -H "Content-Type: application/json" --data "@review.json"
```

**Esperado:** HTTP `202 Accepted`, sem corpo na resposta.

**Teste 10 — Criar uma avaliação com nota inválida**

```powershell
curl.exe -i -X POST http://localhost:8080/reviews -H "Content-Type: application/json" --data "@review-bad.json"
```

**Esperado:** HTTP `400 Bad Request`.

```json
{"error":"Rating must be between 1 and 5"}
```

**Teste 11 — Avaliar um prato inexistente**

```powershell
curl.exe -i -X POST http://localhost:8080/reviews -H "Content-Type: application/json" --data "@review-404.json"
```

**Esperado:** HTTP `404 Not Found`.

```json
{"error":"Dish not found"}
```

**Teste 12 — Consultar o ranking de avaliações**

Aguarde aproximadamente 6 segundos após o teste 9 para permitir o processamento assíncrono da avaliação.

```powershell
curl.exe -i http://localhost:8083/reviews/ranking
```

**Esperado:** HTTP `200 OK`, com o ranking contendo a avaliação enviada.

```json
[
  {
    "dishId": 1,
    "dishName": "Promo Burger",
    "average": 5.0,
    "count": 1
  }
]
```

### 3.4. Assistente com IA

**Teste 13 — Fazer uma pergunta relacionada ao restaurante**

```powershell
curl.exe -i -X POST http://localhost:8080/assistant -H "Content-Type: application/json" --data "@q.json"
```

**Esperado:** HTTP `200 OK`, com uma resposta JSON no formato `{"answer":"..."}`, citando o Veggie Bowl e respondendo à pergunta sobre pratos vegetarianos de até R$ 40.

**Teste 14 — Fazer uma pergunta fora do contexto do restaurante**

```powershell
curl.exe -i -X POST http://localhost:8080/assistant -H "Content-Type: application/json" --data "@q2.json"
```

**Esperado:** HTTP `200 OK`, com uma recusa educada e um redirecionamento para assuntos relacionados ao restaurante.

## 4. Testes extras

### 4.1. Estoque insuficiente — HTTP 409

O prato de ID `1` começa com estoque de 10 unidades.

1. Faça 10 pedidos válidos desse prato, com quantidade `1` cada.
2. Tente criar mais um pedido.

**Esperado:** o pedido excedente retorna HTTP `409 Conflict`.

```json
{"error":"Dish out of stock"}
```

### 4.2. Race condition — requisições simultâneas

Envie 50 requisições simultâneas de pedidos, cada uma solicitando uma unidade do prato de ID `1`, inicialmente com estoque de 10 unidades.

**Esperado:**

- Exatamente 10 pedidos retornam HTTP `201 Created`.
- Os demais pedidos são recusados por falta de estoque.
- O estoque final é `0`.
- Não ocorre estoque negativo.

### 4.3. Payment Service indisponível — HTTP 502

1. Pare as duas instâncias do `payment-service`.
2. Envie uma requisição válida para criar um pedido.
3. Consulte o estoque do prato.

**Esperado:**

```json
{"error":"Payment service unavailable"}
```

A requisição retorna HTTP `502 Bad Gateway`, e o estoque permanece inalterado.

### 4.4. Balanceamento de carga

Com as duas instâncias do `payment-service` em execução, envie várias requisições que acionem o serviço de pagamento e confira os logs de ambas as instâncias.

**Esperado:** as requisições são distribuídas entre as instâncias nas portas `8081` e `8082`, conforme a estratégia de balanceamento configurada.

### 4.5. Registro dos microsserviços no Eureka

Acesse:

- Eureka Dashboard: http://localhost:8761

**Esperado:** os serviços `ORDER-SERVICE`, `REVIEW-SERVICE` e `PAYMENT-SERVICE` aparecem registrados, com duas instâncias de `PAYMENT-SERVICE`.

### 4.6. RabbitMQ

Acesse o painel:

- RabbitMQ Management: http://localhost:15672
- Usuário: `guest`
- Senha: `guest`

Na aba **Queues**, verifique a existência da fila `reviews.queue`.

**Esperado:** a fila `reviews.queue` existe e está configurada como durável.

## 5. Checklist de execução

- [ ] Docker Compose iniciado.
- [ ] Eureka Server iniciado.
- [ ] Duas instâncias do Payment Service iniciadas.
- [ ] Order Service iniciado.
- [ ] Review Service iniciado.
- [ ] Ollama iniciado com o modelo `llama3.2`.
- [ ] Arquivos JSON temporários criados.
- [ ] Testes 1 a 14 executados.
- [ ] Teste de estoque insuficiente executado.
- [ ] Teste de concorrência executado.
- [ ] Teste de indisponibilidade do pagamento executado.
- [ ] Balanceamento de carga verificado.
- [ ] Registro dos microsserviços verificado no Eureka.
- [ ] Fila `reviews.queue` verificada no RabbitMQ.
