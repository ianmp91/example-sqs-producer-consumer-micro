# Microservicio Productor/Consumidor (B)

Este servicio es el iniciador del flujo. Se encarga de generar mensajes con payload encriptado hacia la `cola-aws-sqs-1` y escuchar respuestas procesadas en la `cola-aws-sqs-2`.

## 🔄 Flujo de Mensajería

1.  **Produce (Encryption):** Crea un mensaje con metadata y `rawPayload`.
    * **Seguridad:** El `rawPayload` se **ENCRIPTA** usando una **Clave Pública (RSA)** antes de enviarse.
    * **Destino:** `cola-aws-sqs-1`.
2.  **Consume:** Escucha respuestas resultantes del proceso.
    * **Origen:** `cola-aws-sqs-2`.

## 🛠 Requisitos

* **OS:** macOS (Probado en Tahoe 26.1 en MacBook Pro 2021)
* **Java:** JDK 25
* **Librería Interna:** `sqs-consumer-producer-lib` (A)
* **Infraestructura:** ElasticMQ (Docker)

## 🔑 Configuración de Seguridad

El servicio requiere la ruta a la **Clave Pública** para cifrar los mensajes salientes.

Coloca tu clave en `src/main/resources/keys/public_key.pem` o define la ruta en las variables de entorno:

```bash
export ENCRYPTION_PUBLIC_KEY_PATH=/path/to/public_key.pem
```

## 📦 Instalación para Microservicios
Añade la dependencia a tu archivo `build.gradle` (asumiendo que esta librería está publicada en tu repositorio local o Nexus):

```groovy
repositories {
    mavenLocal() //Agregar al miroservicio
    mavenCentral()
}


dependencies {
    implementation 'com.example.sqslib:sqs-consumer-producer-lib:0.0.1-SNAPSHOT'
}
```
## ⚙️ Configuración (application.yml)

```yaml
server:
  port: 8081
spring:
  application:
    name: sqs-producer-consumer-micro
  main:
    allow-bean-definition-overriding: true
  cloud:
    aws:
      region:
        static: us-east-1 # Una región arbitraria
      # Configuración específica para SQS que apunta a ElasticMQ
      sqs:
        endpoint:
          # Apunta al puerto del contenedor de ElasticMQ
          uri: http://localhost:9324
      credentials:
        # Credenciales dummy que cumplen con el requisito del SDK de AWS
        access-key: dummy
        secret-key: dummy
cola:
  aws:
    sqs:
      producer: "cola-aws-sqs-1"
      consumer: "cola-aws-sqs-2"
```

## 🚀 Ejecución

```bash
make clean
make build
make bootRun
```
