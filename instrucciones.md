INSTRUCCIONES DE EJECUCIÓN Y PRUEBAS DEL SISTEMA BANCO XYZ CLOUD

El presente documento detalla paso a paso las acciones necesarias para compilar, levantar, operar y probar cada uno de los microservicios y componentes del ecosistema Banco XYZ Cloud.

1. REQUISITOS PREVIOS

Para ejecutar el ecosistema en un entorno local de evaluación, se debe contar con las siguientes herramientas instaladas:

Docker Desktop (versión 24.x o superior) con Docker Compose v2 integrado y el servicio de Docker activo.

Java Development Kit (JDK 17) instalado y configurado en las variables de entorno del sistema.

Apache Maven (versión 3.8 o superior) en caso de requerir compilación local independiente.

Postman o cliente HTTP equivalente (como cURL) para la ejecución de pruebas funcionales y de seguridad.

Git para la clonación y control de versiones del repositorio.

2. PUESTA EN MARCHA DEL ECOSISTEMA (DOCKER COMPOSE)

Todos los servicios están configurados para orquestarse en una red interna compartida (banco-network) a través de un único archivo docker-compose.yml en la raíz del proyecto.

Paso 1: Abrir la terminal

Abra una consola de comandos (PowerShell, Command Prompt o Terminal de Linux/macOS) y sitúese en el directorio raíz del proyecto:

cd banco-xyz-cloud


Paso 2: Construir e iniciar los contenedores

Ejecute el siguiente comando para compilar las imágenes Docker de cada microservicio e iniciar los contenedores en segundo plano:

docker compose up -d --build


Paso 3: Verificar el estado de los contenedores

Asegúrese de que los 7 servicios se encuentren en estado de ejecución ("Up" o "running"):

docker compose ps


Debe validar la presencia y puertos de los siguientes servicios:

auth-service: Puerto 9000

account-service: Puerto 8081

transfer-service: Puerto 8082

batch-service: Puerto 8083

customer-service: Puerto 8084

bff-service: Puerto 8080

kafka: Puerto 9092

3. PASO A PASO PARA PROBAR CADA COMPONENTE

Las pruebas deben realizarse en el orden descrito para respetar el flujo de autenticación y dependencias de datos.

ETAPA 1: Autenticación y Emisión de Token OAuth2 (auth-service)

Este paso genera la credencial JWT requerida para consultar los endpoints protegidos.

Método HTTP: POST

URL: http://localhost:9000/oauth2/token

Configuración de Autorización: Basic Auth

Username: cliente-app

Password: secreto

Encabezados (Headers):

Content-Type: application/x-www-form-urlencoded

Cuerpo de la petición (Body): Formato x-www-form-urlencoded

grant_type: client_credentials

scope: read write

Respuesta Esperada: Código 200 OK con un JSON que incluye el atributo access_token.

Acción obligatoria: Copiar el texto completo de access_token para las pruebas posteriores.

ETAPA 2: Procesamiento Masivo por Lotes (batch-service)

El servicio de procesamiento en lote expone 3 endpoints REST para ejecutar los trabajos financieros bajo demanda.

Prueba 2.1: Job de Transacciones Diarias

Método HTTP: POST

URL: http://localhost:8083/api/jobs/transacciones-diarias

Cuerpo: Vacío

Respuesta Esperada: Código 200 OK con mensaje de confirmación y "status": "COMPLETED".

Prueba 2.2: Job de Liquidación de Intereses

Método HTTP: POST

URL: http://localhost:8083/api/jobs/intereses-mensuales

Cuerpo: Vacío

Respuesta Esperada: Código 200 OK con mensaje de confirmación y "status": "COMPLETED".

Prueba 2.3: Job de Generación de Estados de Cuenta

Método HTTP: POST

URL: http://localhost:8083/api/jobs/estados-cuenta

Cuerpo: Vacío

Respuesta Esperada: Código 200 OK con mensaje de confirmación y "status": "COMPLETED".

ETAPA 3: Maestro de Clientes (customer-service)

Permite consultar la persistencia de clientes bancarios.

Método HTTP: GET

URL: http://localhost:8084/api/customers

Configuración de Autorización: No Auth (Público)

Respuesta Esperada: Código 200 OK con la lista de clientes registrados en formato JSON.

ETAPA 4: Servicio de Cuentas Bancarias (account-service)

Este microservicio requiere el Bearer Token obtenido en la Etapa 1.

Prueba 4.1: Listado general de cuentas

Método HTTP: GET

URL: http://localhost:8081/api/accounts

Configuración de Autorización: Bearer Token (pegar el token generado)

Respuesta Esperada: Código 200 OK con el listado de cuentas disponibles (por ejemplo, cuenta ID 137).

Prueba 4.2: Detalle de cuenta específica

Método HTTP: GET

URL: http://localhost:8081/api/accounts/137

Configuración de Autorización: Bearer Token (pegar el token generado)

Respuesta Esperada: Código 200 OK con el detalle de saldo, titular y tipo de producto de la cuenta 137.

ETAPA 5: Adaptación Multicanal y Fachada (bff-service)

El servicio BFF recibe el Bearer Token en su cabecera y lo propaga internamente a account-service, entregando respuestas adaptadas para cada canal cliente.

Prueba 5.1: Canal Móvil

Método HTTP: GET

URL: http://localhost:8080/api/bff/mobile/cuentas/137

Configuración de Autorización: Bearer Token (pegar el token generado)

Respuesta Esperada: Código 200 OK con la estructura de datos optimizada para dispositivos móviles.

Prueba 5.2: Canal Web

Método HTTP: GET

URL: http://localhost:8080/api/bff/web/cuentas/137

Configuración de Autorización: Bearer Token (pegar el token generado)

Respuesta Esperada: Código 200 OK con el payload extendido para navegación web.

Prueba 5.3: Canal Cajero Automático (ATM)

Método HTTP: GET

URL: http://localhost:8080/api/bff/atm/cuentas/137

Configuración de Autorización: Bearer Token (pegar el token generado)

Respuesta Esperada: Código 200 OK con datos filtrados para terminales de autoservicio.

4. DETENCIÓN Y LIMPIEZA DEL ENTORNO

Para finalizar la ejecución del ecosistema y liberar los recursos del sistema anfitrión, ejecute:

docker compose down


Si desea eliminar también los volúmenes de datos asociados para reiniciar las pruebas desde cero:

docker compose down -v
