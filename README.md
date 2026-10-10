# Banco XYZ - Arquitectura Cloud Native y Microservicios

Sistema transaccional bancario distribuido basado en arquitectura de microservicios con **Spring Boot**, **OAuth 2.0 / JWT**, **Backend for Frontend (BFF)**, **Spring Batch**, **Resilience4j**, **Apache Kafka** y orquestación integral en contenedores con **Docker Compose**.

---

## 🏛️ Arquitectura del Sistema

El ecosistema está compuesto por siete componentes desplegados en una red interna (`banco-network`):

```
                               [ Clientes Multicanal / Postman ]
                                              │
                                              ▼
                                  ┌───────────────────────┐
                                  │      bff-service      │ (Puerto 8080)
                                  │  (Web / Móvil / ATM)  │
                                  └───────────┬───────────┘
                                              │
             ┌────────────────────────────────┼────────────────────────────────┐
             │ (Propagación Bearer Token)     │                                │
             ▼                                ▼                                ▼
   ┌───────────────────┐            ┌───────────────────┐            ┌───────────────────┐
   │   auth-service    │            │  account-service  │            │ customer-service  │
   │  (Puerto 9000)    │            │   (Puerto 8081)   │            │   (Puerto 8084)   │
   │ (OAuth2 / JWKS)   │            │  (JPA / H2 / CSV) │            │ (Maestro Clientes)│
   └───────────────────┘            └─────────▲─────────┘            └───────────────────┘
                                              │
                                              │ (Verificación de Saldo / CB)
                                              │
                                    ┌─────────┴─────────┐            ┌───────────────────┐
                                    │ transfer-service  │            │   batch-service   │
                                    │   (Puerto 8082)   │            │   (Puerto 8083)   │
                                    │  (Orquestador TX) │            │  (Spring Batch)   │
                                    └─────────┬─────────┘            └───────────────────┘
                                              │
                                              │ (Eventos Asíncronos)
                                              ▼
                                    ┌───────────────────┐
                                    │   Apache Kafka    │
                                    │   (Puerto 9092)   │
                                    │   (Modo KRaft)    │
                                    └───────────────────┘
```

### Componentes del Ecosistema

1. **`bff-service` (Puerto 8080):** Capa de agregación y fachada multicanal (Backend for Frontend). Adapta y optimiza las respuestas para clientes Web, Móvil y Cajeros Automáticos (ATM), propagando los tokens OAuth2 hacia los microservicios internos.
2. **`auth-service` (Puerto 9000):** Servidor centralizado de autorización bajo OAuth 2.0 (Spring Authorization Server). Emite tokens JWT firmados asimétricamente (RS256) con scopes bancarios (`banco.read`, `banco.write`, `read`, `write`).
3. **`account-service` (Puerto 8081):** Microservicio de gestión de cuentas bancarias. Implementa persistencia relacional en memoria (H2), carga/saneamiento de datos legados vía CSV y endpoints de consulta protegidos.
4. **`customer-service` (Puerto 8084):** Microservicio encargado del catálogo maestro de clientes bancarios, con endpoints REST protegidos y base de datos desacoplada.
5. **`batch-service` (Puerto 8083):** Procesamiento masivo de datos mediante Spring Batch. Ejecuta jobs automatizados para conciliación de transacciones diarias, cálculo de intereses y emisión de estados de cuenta.
6. **`transfer-service` (Puerto 8082):** Orquestador transaccional que valida fondos, ejecuta débitos/créditos, publica eventos asíncronos y aplica tolerancia a fallos mediante Circuit Breaker.
7. **`kafka` (Puerto 9092):** Broker de mensajería distribuida en modo KRaft que gestiona los tópicos de auditoría y transferencias.

---

## 🚀 Requisitos e Integraciones Principales

- **Seguridad OAuth 2.0 / JWT:** Arquitectura centralizada de autenticación. Los microservicios operan como Resource Servers (`@PreAuthorize`) validando firmas mediante JWKS.
- **Patrón Backend for Frontend (BFF):** Consolidación de canales Web, Mobile y ATM con propagación segura del header `Authorization`.
- **Procesamiento Batch:** Arquitectura Chunk-oriented con Spring Batch para transacciones y conciliaciones de alto volumen.
- **Tolerancia a Fallos (Resilience4j):** Patrón **Circuit Breaker** y **Fallback** en `transfer-service` ante degradación de `account-service`.
- **Arquitectura Basada en Eventos:** Patrón **Producer / Consumer** sobre Apache Kafka (`banco.transferencias`) para auditoría y desacoplamiento.
- **Contenerización Total:** Dockerfiles multi-stage y orquestación unificada mediante Docker Compose.

---

## 🛠️ Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3** (Web, Security, OAuth2 Resource Server, Data JPA, Spring Batch)
- **Resilience4j** (Circuit Breaker, TimeLimiter)
- **Apache Kafka & Spring Kafka** (Confluent Platform en modo KRaft)
- **H2 Database** (Persistencia relacional en memoria)
- **Docker & Docker Compose**

---

## 📂 Estructura del Repositorio

```text
banco-xyz-cloud/
│
├── auth-service/           # Servidor de Autorización OAuth 2.0
├── bff-service/            # Fachada y adaptación de canales (Web, Mobile, ATM)
├── account-service/        # Cuentas bancarias y saneamiento CSV
├── customer-service/       # Maestro de clientes bancarios
├── batch-service/          # Procesos batch y tareas programadas
├── transfer-service/       # Orquestación de transferencias y Kafka
├── docker-compose.yml      # Orquestación de los 7 contenedores
├── instrucciones.md        # Guía paso a paso para pruebas locales
├── despliegue.md           # Guía de despliegue en nube empresarial
└── README.md               # Documentación general del ecosistema
```

---

## ⚙️ Despliegue y Ejecución

### Prerrequisitos
* Docker y Docker Desktop activos.

### 1. Construir e iniciar contenedores
Desde la raíz del proyecto, ejecuta el despliegue orquestado en segundo plano:
```bash
docker compose up --build -d
```

### 2. Verificar estado de los servicios
```bash
docker compose ps
```
Los 7 contenedores (`auth-service`, `account-service`, `transfer-service`, `kafka`, `bff-service`, `customer-service`, `batch-service`) deben reportar estado `Up`.

---

## 🧪 Pruebas de API y Validación

### 1. Obtener Token de Acceso (OAuth 2.0)
* **Método:** `POST`
* **URL:** `http://localhost:9000/oauth2/token`
* **Authorization:** `Basic Auth`
  * Username: `banco-client`
  * Password: `banco-secret`
* **Body (`x-www-form-urlencoded`):**
  * `grant_type`: `client_credentials`
  * `scope`: `banco.read banco.write`

---

### 2. Adaptacion de Canales BFF
* **Canal Móvil:** GET http://localhost:8080/api/bff/mobile/cuentas/137

* **Canal Web:** GET http://localhost:8080/api/bff/web/cuentas/137

* **Canal ATM:** GET http://localhost:8080/api/bff/atm/cuentas/137

* **Authorization:** Bearer Token <ACCESS_TOKEN>

---

### 3. Maestro de Clientes
* **Listar Clientes:** GET http://localhost:8084/api/customers

---

### 4. Procesamiento Batch
* **Transacciones Diarias:** POST http://localhost:8083/api/jobs/transacciones-diarias

* **Intereses Mensuales:** POST http://localhost:8083/api/jobs/intereses-mensuales

* **Estados de Cuenta:** POST http://localhost:8083/api/jobs/estados-cuenta

--


### 5. Consultar Saldo Inicial
* **Método:** `GET`
* **URL:** `http://localhost:8081/api/accounts/137`
* **Authorization:** `Bearer Token` `<TU_ACCESS_TOKEN>`

---

### 6. Procesar Transferencia Transaccional
* **Método:** `POST`
* **URL:** `http://localhost:8082/api/transfers`
* **Authorization:** `Bearer Token` `<TU_ACCESS_TOKEN>`
* **Headers:** `Content-Type: application/json`
* **Body (`raw JSON`):**
```json
{
  "sourceAccountId": "137",
  "targetAccountId": "133",
  "amount": 2000.0,
  "description": "Pago de honorarios"
}
```

---

### 7. Verificar Eventos Asíncronos en Apache Kafka
Revisa los logs del contenedor para constatar la publicación del evento y el consumo de auditoría:
```bash
docker compose logs transfer-service --tail=30
```
Trazas esperadas:
```text
==> [KAFKA PRODUCER] Publicando evento de transferencia en tópico 'banco.transferencias': {"transactionId":"TX-...","source":"137","target":"133","amount":2000.00,"status":"COMPLETED"}
==> [KAFKA CONSUMER - AUDITORÍA] Evento de transferencia recibido con éxito: ...
```

---

### 8. Demostración de Resiliencia (Circuit Breaker con Resilience4j)

1. Detén el microservicio de cuentas:
   ```bash
   docker compose stop account-service
   ```
2. Ejecuta nuevamente la transferencia en Postman (`POST http://localhost:8082/api/transfers`).
3. El circuito se abre y el método **Fallback** responde con estado controlado `503 Service Unavailable`:
   ```json
   {
     "status": "CIRCUIT_BREAKER_FALLBACK",
     "error": "Servicio de cuentas temporalmente no disponible (Circuito Abierto por Resiliencia). Reintente en unos instantes.",
     "timestamp": "..."
   }
   ```
4. Reactiva el microservicio:
   ```bash
   docker compose start account-service
   ```
   El circuito se restablece automáticamente sin necesidad de reiniciar la aplicación.

---

## 🛑 Detener la Plataforma

Para detener y limpiar los contenedores y redes virtuales del proyecto:
```bash
docker compose down
```