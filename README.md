# Oracle Task Manager - File Service

Este repositorio contiene el microservicio de gestión de archivos para el sistema Oracle Task Manager. Se encarga de manejar los archivos adjuntos a las tareas, incluyendo su almacenamiento en Oracle Object Storage.

## Requisitos previos

* Java JDK 23
* Maven
* Oracle Wallet (proporcionado separadamente)
* Git
* Clave privada (PEM) para el acceso a Oracle Object Storage

## Configuración del entorno local

### 1. Clonar el repositorio

```bash
git clone https://github.com/tuOrganizacion/FileService.git
cd FileService
```

### 2. Configurar Oracle Wallet

#### 2.1 Configurar `sqlnet.ora`

Asegúrate de que el archivo `sqlnet.ora` ubicado dentro del directorio del wallet contenga la ruta correcta. Ejemplo:

```ora
WALLET_LOCATION = (SOURCE = (METHOD = file) (METHOD_DATA = (DIRECTORY="C:\Users\your-username\Wallet_TelegramBotDatabase")))
SSL_SERVER_DN_MATCH=yes
```

#### 2.2 Configurar `application.properties`

1. Coloca los archivos del wallet en una ubicación accesible (p. ej., `C:/Users/your-username/Wallet_TelegramBotDatabase`).

2. Configura `application.properties` con valores genéricos:

```properties
spring.application.name=FileService
spring.datasource.url=jdbc:oracle:thin:@TelegramBotDatabase_medium?TNS_ADMIN=C:/Users/your-username/Wallet_TelegramBotDatabase
spring.datasource.username=ADMIN
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
jwt.secret.oracle=${JWT_SECRET_ORACLE}
spring.jackson.time-zone=America/Mexico_City
server.port=8082

spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.max-lifetime=600000

# Swagger
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Estas líneas deben comentarse en desarrollo local
springdoc.swagger-ui.configUrl=/swagger-files/v3/api-docs/swagger-config
springdoc.swagger-ui.url=/swagger-files/v3/api-docs
springdoc.swagger-ui.oauth2RedirectUrl=/swagger-files/swagger-ui/oauth2-redirect.html
springdoc.swagger-ui.disable-swagger-default-url=true

# Oracle Cloud Object Storage
oci.user-id=${OCI_USER_ID}
oci.tenancy-id=${OCI_TENANCY_ID}
oci.compartment-id=${OCI_COMPARTMENT_ID}
oci.fingerprint=${OCI_FINGERPRINT}
oci.region=mx-queretaro-1
oci.private-key-path=${OCI_PRIVATE_KEY_PATH}
oci.bucket-name=task-attachments
oci.namespace=${OCI_NAMESPACE}
oci.preauthenticated-request-name=${OCI_PRESIGNED_URL}
```

3. Configura las siguientes variables de entorno:

* `DB_PASSWORD`: Contraseña de la base de datos
* `JWT_SECRET_ORACLE`: Clave secreta para generación de tokens
* `OCI_PRIVATE_KEY_PATH`: Ruta al archivo `.pem` de tu clave privada de OCI
* `OCI_USER_ID`, `OCI_TENANCY_ID`, `OCI_COMPARTMENT_ID`, `OCI_FINGERPRINT`, `OCI_NAMESPACE`, `OCI_PRESIGNED_URL`: Datos de configuración de OCI

> 🔒 **Nunca subas estos valores al repositorio. Usa variables de entorno o un gestor de secretos.**

### 3. Compilar y ejecutar la aplicación

```bash
mvn clean package
java -jar target/FileService-0.0.1-SNAPSHOT.jar
```

O bien, ejecuta directamente con Maven:

```bash
mvn spring-boot:run
```

### 4. Verificar la instalación

* API disponible en: [http://localhost:8082](http://localhost:8082)
* Documentación Swagger UI: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

## Diferencias entre entorno local y producción

### Configuración del Wallet

| Local                                 | Producción                                     |
| ------------------------------------- | ---------------------------------------------- |
| Ruta local al wallet (`C:/Users/...`) | Montado como volumen en contenedor (`/wallet`) |

### Configuración de Swagger

| Local                      | Producción                                                   |
| -------------------------- | ------------------------------------------------------------ |
| Acceso: `/swagger-ui.html` | Acceso: `http://140.84.189.81/swagger-files/swagger-ui/index.html` |
| Sin prefijo en las URLs    | Prefijo `/api/files` en producción                           |
| No requiere configUrl      | Requiere `springdoc.swagger-ui.configUrl` configurado        |

### Configuración de endpoints

| Local                                | Producción                                    |
| ------------------------------------ | --------------------------------------------- |
| `http://localhost:8082/files/upload` | `http://140.84.189.81/api/files/files/upload` |
| Sin proxy                            | Usa Ingress con rutas reescritas              |

### Despliegue con GitHub Actions

El pipeline de producción:

1. Reemplaza configuraciones para usar la ruta `/wallet`
2. Compila con Maven
3. Construye imagen Docker
4. Sube la imagen al Oracle Container Registry
5. Reconstruye el wallet y la clave privada desde secretos
6. Despliega en Kubernetes

Todo el proceso es automatizado por `.github/workflows/build-push-files.yml` al hacer push a la rama principal.

## Ejemplo de petición (Local)

```http
POST http://localhost:8082/files/upload
Content-Type: multipart/form-data
Authorization: Bearer <TOKEN_JWT>

<archivo_adjunto>
```

## Documentación API

* **Local**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
* **Producción**: [http://140.84.189.81/swagger-files/swagger-ui/index.html](http://140.84.189.81/swagger-files/swagger-ui/index.html)

## Solución de problemas

### Error de conexión a la base de datos

Verifica:

* Archivos del wallet completos
* Ruta `TNS_ADMIN` correcta
* Credenciales válidas

### Problemas con Object Storage

Verifica:

* Que la clave privada esté en la ruta definida
* Que los OCIDs estén correctos
* Que la URL prefirmada (si aplica) sea válida

### Swagger no carga

* Asegúrate de comentar `configUrl`, `url` y demás en local
* Verifica que `springdoc.swagger-ui.enabled=true`

---
