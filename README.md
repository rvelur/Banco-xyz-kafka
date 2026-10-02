# Banco XYZ - Arquitectura Cloud Native y Microservicios

Sistema transaccional bancario distribuido basado en arquitectura de microservicios con **Spring Boot**, **OAuth 2.0**, **Resilience4j**, **Apache Kafka** y orquestación en contenedores con **Docker Compose**.

---

## 🏛️ Arquitectura del Sistema

El ecosistema está compuesto por cuatro componentes desplegados en una red interna (`banco-network`):

```
                        [ Cliente / Postman ]
                                  │
                   (Bearer JWT)   │   (Solicitud TX)
                                  ▼
 ┌─────────────────┐     ┌───────────────────┐     ┌─────────────────┐
 │   auth-service  │◄────│  transfer-service │────►│ account-service │
 │ (OAuth2 Server) │     │   (Orquestador)   │ CB  │ (JPA / H2 / CSV)│
 └─────────────────┘     └─────────┬─────────┘     └─────────────────┘
                                   │
                                   │ (Publicación de Eventos)
                                   ▼
                         ┌───────────────────┐
                         │   Apache Kafka    │
                         │   (Modo KRaft)    │
                         └───────────────────┘
```

1. **`auth-service` (Puerto 9000):** Servidor de autorización centralizado bajo OAuth 2.0 (Spring Authorization Server). Emite tokens JWT firmados asimétricamente (RS256) con scopes bancarios (`banco.read`, `banco.write`).
2. **`account-service` (Puerto 8081):** Microservicio de gestión de cuentas bancarias. Implementa persistencia relacional en memoria (H2) y proceso automatizado de ingesta/saneamiento de datos legados vía archivo CSV.
3. **`transfer-service` (Puerto 8082):** Orquestador transaccional que valida fondos, debita en origen, acredita en destino, publica eventos asíncronos y protege la comunicación mediante tolerancia a fallos.
4. **`kafka` (Puerto 9092):** Broker de mensajería distribuida en modo KRaft que recibe y distribuye los eventos de auditoría y transferencias completadas.

---

## 🚀 Requisitos e Integraciones Principales

* **Seguridad OAuth 2.0 / JWT:** Protección de endpoints con Resource Server (`@PreAuthorize`) y validación de scopes bancarios.
* **Ingesta Legacy:** Carga y saneamiento de registros contables al inicio del ciclo de vida (`CsvAccountLoader`).
* **Tolerancia a Fallos (Resilience4j):** Patrón **Circuit Breaker** y **Fallback** implementado en `AccountClientService` para mitigar indisponibilidad o latencia en el servicio de cuentas.
* **Arquitectura Orientada a Eventos:** Patrón **Producer / Consumer** mediante **Apache Kafka** sobre el tópico `banco.transferencias` para auditoría y desacoplamiento transaccional.
* **Contenerización:** `Dockerfile` multi-stage para cada microservicio y orquestación centralizada vía `docker-compose.yaml`.

---

## 🛠️ Tecnologías Utilizadas

* **Java 17**
* **Spring Boot 3** (Web, Security, OAuth2 Resource Server, Data JPA)
* **Resilience4j** (Circuit Breaker, AOP)
* **Apache Kafka & Spring Kafka** (Confluent Platform 7.5.0 en modo KRaft)
* **H2 Database**
* **Docker & Docker Compose**

---

## 📂 Estructura del Repositorio

```text
banco-xyz-cloud/
│
├── auth-service/           # Servidor de Autorización OAuth 2.0
├── account-service/        # Gestión de cuentas y saneamiento CSV
├── transfer-service/       # Orquestador, Resiliencia y Eventos Kafka
├── docker-compose.yaml     # Orquestación de infraestructura y microservicios
├── .gitignore              # Exclusiones de Git
└── README.md               # Documentación técnica
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
Los 4 contenedores (`auth-service`, `account-service`, `transfer-service` y `kafka`) deben reportar estado `Up`.

---

## 🧪 Pruebas de API y Validación

### 1. Obtener Token de Acceso (OAuth 2.0)
* **Método:** `POST`
* **URL:** `http://localhost:9000/oauth2/token`
* **Authorization:** `Basic Auth`
  * Username: `backend-client`
  * Password: `secret-password`
* **Body (`x-www-form-urlencoded`):**
  * `grant_type`: `client_credentials`
  * `scope`: `banco.read banco.write`

---

### 2. Consultar Saldo Inicial
* **Método:** `GET`
* **URL:** `http://localhost:8081/api/accounts/137`
* **Authorization:** `Bearer Token` `<TU_ACCESS_TOKEN>`

---

### 3. Procesar Transferencia Transaccional
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

### 4. Verificar Eventos Asíncronos en Apache Kafka
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

### 5. Demostración de Resiliencia (Circuit Breaker con Resilience4j)

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