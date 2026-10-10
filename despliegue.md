GUÍA TÉCNICA DE DESPLIEGUE EN LA NUBE - BANCO XYZ CLOUD

El presente documento describe la estrategia, herramientas, arquitectura y procedimiento técnico detallado para desplegar el ecosistema de microservicios de Banco XYZ Cloud en un entorno de computación en la nube empresarial.

1. SELECCIÓN DE PROVEEDOR Y HERRAMIENTAS DE NUBE

Para garantizar alta disponibilidad, aislamiento de red y escalabilidad elástica, la solución está diseñada para ejecutarse sobre Amazon Web Services (AWS) utilizando las siguientes tecnologías:

Amazon Elastic Container Registry (Amazon ECR): Registro privado y gestionado de imágenes Docker para almacenar de forma segura las versiones construidas de cada microservicio.

Amazon Elastic Kubernetes Service (Amazon EKS) o Amazon ECS con AWS Fargate: Orquestador de contenedores sin servidor que permite ejecutar cada microservicio de manera desacoplada sin gestionar servidores subyacentes.

Amazon Managed Streaming for Apache Kafka (Amazon MSK): Servicio gestionado de Apache Kafka que asegura tolerancia a fallos y alta tasa de transferencia sin requerir mantenimiento operativo del broker.

Amazon RDS (PostgreSQL/Aurora) o DynamoDB: Reemplazo en la nube de las bases de datos locales H2, garantizando persistencia transaccional multi-zona, respaldos automáticos y encriptación en reposo.

AWS Application Load Balancer (ALB): Balanceador de carga de capa 7 para exponer externamente el punto de entrada (BFF y Auth Server) con terminación SSL/TLS (HTTPS) y certificados gestionados mediante AWS Certificate Manager (ACM).

AWS Secrets Manager y Systems Manager Parameter Store: Almacenamiento seguro de secretos bancarios, credenciales de base de datos y llaves de firma criptográfica de tokens JWT.

Amazon CloudWatch: Centralización de métricas de rendimiento, dashboards de salud de contenedores y agregación de logs de aplicación.

2. PREPARACIÓN Y CONFIGURACIÓN PREVIA

Antes de iniciar el despliegue, deben ejecutarse las siguientes configuraciones en la cuenta de nube:

Configuración de Red Virtual (Amazon VPC):

Creación de una VPC con mínimo 2 zonas de disponibilidad.

Configuración de subredes públicas para el balanceador de carga ALB y puertas de enlace NAT (NAT Gateway).

Configuración de subredes privadas sin salida directa a Internet para alojar los microservicios, brokers de mensajería y bases de datos.

Creación de Repositorios en Amazon ECR:

Crear un repositorio para cada microservicio del sistema:

banco-xyz/auth-service

banco-xyz/account-service

banco-xyz/transfer-service

banco-xyz/batch-service

banco-xyz/customer-service

banco-xyz/bff-service

Creación de Roles y Políticas IAM:

Asignar un rol de ejecución (Task Execution Role) con permisos mínimos indispensables para descargar imágenes desde ECR y registrar métricas en CloudWatch Logs.

3. PROCESO PASO A PASO DE DESPLIEGUE

Paso 1: Autenticación en el registro de contenedores (ECR)

Desde la máquina local o el servidor de Integración Continua (CI/CD), autentique el cliente Docker con el registro de AWS ECR:

aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ID_CUENTA_AWS>.dkr.ecr.us-east-1.amazonaws.com


Paso 2: Construcción y etiquetado de imágenes Docker

Construya y etiquete la imagen de producción de cada servicio. Ejemplo para auth-service y bff-service:

docker build -t banco-xyz/auth-service ./auth-service
docker tag banco-xyz/auth-service:latest <ID_CUENTA_AWS>.dkr.ecr.us-east-1.amazonaws.com/banco-xyz/auth-service:v1.0

docker build -t banco-xyz/bff-service ./bff-service
docker tag banco-xyz/bff-service:latest <ID_CUENTA_AWS>.dkr.ecr.us-east-1.amazonaws.com/banco-xyz/bff-service:v1.0


(Repetir el proceso con los módulos restantes: account-service, transfer-service, batch-service y customer-service).

Paso 3: Publicación de imágenes en la nube (Docker Push)

Suba las imágenes etiquetadas hacia sus repositorios correspondientes en ECR:

docker push <ID_CUENTA_AWS>.dkr.ecr.us-east-1.amazonaws.com/banco-xyz/auth-service:v1.0
docker push <ID_CUENTA_AWS>.dkr.ecr.us-east-1.amazonaws.com/banco-xyz/bff-service:v1.0


Paso 4: Ajuste de Variables de Entorno y Configuración en la Nube

En el entorno productivo de ECS o Kubernetes, las variables de entorno de cada microservicio se desacoplan del código fuente y se inyectan en la definición de la tarea (Task Definition):

AUTH_SERVER_URL: Se configura con el nombre DNS interno del servicio de autenticación o el balanceador privado (http://auth-service.internal:9000).

SPRING_KAFKA_BOOTSTRAP_SERVERS: Se configura apuntando al endpoint generado por Amazon MSK en lugar de localhost:9092.

SPRING_DATASOURCE_URL: Se reemplaza la base en memoria por la cadena de conexión de Amazon RDS PostgreSQL en alta disponibilidad.

Paso 5: Despliegue de los Servicios en el Clúster

Se crea el clúster gestionado en ECS (o EKS).

Se despliegan primero los servicios de infraestructura base (Kafka y Base de Datos relacional).

Se despliegan los microservicios de dominio en subredes privadas (account-service, customer-service, batch-service, transfer-service).

Se despliega auth-service y bff-service conectándolos como destinos del Balanceador de Carga de Aplicaciones (ALB).

Paso 6: Configuración del Balanceador de Carga y Reglas de Enrutamiento

El Application Load Balancer se configura con reglas de enrutamiento basadas en rutas (Path-based routing) protegidas por HTTPS:

Todas las peticiones con prefijo /oauth2/* se redirigen al grupo de destino de auth-service.

Todas las peticiones con prefijo /api/bff/* se redirigen al grupo de destino de bff-service.

Los microservicios de dominio (account-service, customer-service, batch-service) no quedan expuestos a Internet público, mitigando vectores de ataque externos y garantizando que toda interacción pase por el BFF o la autenticación OAuth2.

4. ESTRATEGIA DE ESCALABILIDAD, RESILIENCIA Y MONITOREO

Autoescalado Automático (Auto Scaling): Se configuran políticas de escalado basadas en el uso de CPU y memoria (umbral de 70%) para que servicios de alta demanda como bff-service escalen horizontalmente en momentos de alta concurrencia.

Salud del Sistema (Health Checks): Cada contenedor implementa los endpoints de Spring Boot Actuator (/actuator/health), permitiendo que el orquestador reemplace de forma automática cualquier contenedor con anomalías sin afectar al usuario final.

Monitoreo Centralizado: Los registros de salida estándar (stdout) de cada microservicio se envían en tiempo real a Amazon CloudWatch Logs mediante el driver awslogs, habilitando trazabilidad distribuida y alertas de error en caso de excepciones 5xx.