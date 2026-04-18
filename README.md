# Franchise API

REST API reactiva para gestión de franquicias, sucursales y productos. Desarrollada como prueba técnica para Accenture.

## Stack tecnológico

| Tecnología | Detalle |
|---|---|
| Java 17 | Lenguaje |
| Spring Boot 3.2.3 WebFlux | Framework reactivo (Project Reactor) |
| AWS DynamoDB | Base de datos NoSQL |
| AWS SDK v2 | Cliente DynamoDB Enhanced (async) |
| Terraform | Infraestructura como código |
| Docker + docker-compose | Empaquetado y ejecución local |
| Maven 3.9 | Gestión de dependencias |

## Arquitectura

El proyecto implementa **arquitectura hexagonal** (puertos y adaptadores):

```
src/main/java/com/accenture/franchiseapi/
├── domain/
│   └── model/                  # Entidades puras (Franchise, Branch, Product)
├── application/
│   ├── port/
│   │   ├── in/                 # Puerto de entrada (FranchiseUseCase)
│   │   └── out/                # Puerto de salida (FranchiseRepository)
│   └── service/                # Lógica de negocio (FranchiseService)
└── infrastructure/
    ├── adapter/
    │   ├── in/web/             # Controlador REST + DTOs
    │   └── out/dynamodb/       # Repositorio DynamoDB + entidades + mapper
    ├── config/                 # DynamoDbConfig + DynamoDbTableInitializer
    └── exception/              # GlobalExceptionHandler + excepciones custom
```

## Endpoints

### Criterios de aceptación

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/franchises` | Agregar nueva franquicia |
| `POST` | `/api/franchises/{franchiseId}/branches` | Agregar sucursal a una franquicia |
| `POST` | `/api/franchises/{franchiseId}/branches/{branchId}/products` | Agregar producto a una sucursal |
| `DELETE` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar producto de una sucursal |
| `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar stock de un producto |
| `GET` | `/api/franchises/{franchiseId}/top-products` | Producto con mayor stock por sucursal |

### Puntos extra

| Método | URL | Descripción |
|--------|-----|-------------|
| `PATCH` | `/api/franchises/{franchiseId}/name` | Actualizar nombre de franquicia |
| `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/name` | Actualizar nombre de sucursal |
| `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Actualizar nombre de producto |

## Prerrequisitos

- Java 17+
- Maven 3.9+
- Docker y docker-compose
- Terraform 1.0+ (para despliegue en AWS)
- AWS CLI configurado (para despliegue en AWS)

## Ejecución local con Docker

La opción más rápida: levanta la aplicación junto con DynamoDB Local (sin necesidad de cuenta AWS).

```bash
docker-compose up --build
```

La API quedará disponible en `http://localhost:8080`.  
DynamoDB Local corre en `http://localhost:8000`.

La tabla `franchises` se crea automáticamente al iniciar la aplicación.

Para detener:

```bash
docker-compose down
```

## Despliegue en AWS

### 1. Configurar credenciales AWS

```bash
aws configure
```

Ingresa tu `AWS Access Key ID`, `AWS Secret Access Key` y región (`us-east-1`).

### 2. Crear la tabla DynamoDB con Terraform

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

Esto crea la tabla `franchises` en DynamoDB con billing `PAY_PER_REQUEST`.

### 3. Ejecutar la aplicación apuntando a AWS

```bash
mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Daws.region=us-east-1 -Daws.dynamodb.table-name=franchises"
```

La aplicación detecta automáticamente las credenciales configuradas en el AWS CLI mediante `DefaultCredentialsProvider`.

### 4. Destruir infraestructura (opcional)

```bash
cd terraform
terraform destroy
```

## Ejemplos de uso

### Agregar franquicia

```bash
curl -X POST http://localhost:8080/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "McDonalds"}'
```

### Agregar sucursal

```bash
curl -X POST http://localhost:8080/api/franchises/{franchiseId}/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Norte"}'
```

### Agregar producto

```bash
curl -X POST http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Big Mac", "stock": 100}'
```

### Modificar stock

```bash
curl -X PATCH http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 200}'
```

### Producto con mayor stock por sucursal

```bash
curl http://localhost:8080/api/franchises/{franchiseId}/top-products
```

Respuesta de ejemplo:

```json
[
  {
    "branchId": "abc123",
    "branchName": "Sucursal Norte",
    "productId": "xyz789",
    "productName": "Big Mac",
    "stock": 200
  }
]
```

### Eliminar producto

```bash
curl -X DELETE http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}
```

## Variables de configuración

| Variable | Por defecto | Descripción |
|---|---|---|
| `aws.region` | `us-east-1` | Región de AWS |
| `aws.dynamodb.endpoint` | *(vacío — usa AWS real)* | Endpoint custom (ej. DynamoDB Local) |
| `aws.dynamodb.table-name` | `franchises` | Nombre de la tabla DynamoDB |
| `aws.access-key` | *(vacío)* | Access key estática (opcional) |
| `aws.secret-key` | *(vacío)* | Secret key estática (opcional) |
