# Producer/Consumer Microservice (B)

This service is the flow initiator. It is responsible for generating messages with encrypted payload to `cola-aws-sqs-1` and listening for processed responses on `cola-aws-sqs-2`.

## 🔄 Messaging Flow

1. **Produce (Encryption):** Creates a message with metadata and `rawPayload`.
    * **Security:** The `rawPayload` is **ENCRYPTED** using a **Public Key (RSA)** before being sent.
    * **Destination:** `cola-aws-sqs-1`.

2. **Consume:** Listens for responses resulting from the process.
    * **Source:** `cola-aws-sqs-2`.

## 🛠 Requirements

* **OS:** macOS (Tested on Tahoe 26.1 on MacBook Pro 2021)
* **Java:** JDK 25
* **Infrastructure:** ElasticMQ (Docker)
* **Internal Library:** `sqs-consumer-producer-lib` (A)

## 🔑 Security Configuration

The service requires the path to the **Public Key** to encrypt outgoing messages.

Place your key in `src/main/resources/keys/public_key.pem` or define the path in environment variables:

```bash
export ENCRYPTION_PUBLIC_KEY_PATH=/path/to/public_key.pem
```

## 📦 Installation for Microservices

Add the dependency to your `build.gradle` file (assuming this library is published in your local repository).

```groovy
repositories {
    mavenLocal() // Add to the microservice
    mavenCentral()
}


dependencies {
    implementation 'com.example.sqslib:sqs-consumer-producer-lib:0.0.1-SNAPSHOT'
}
```

## ⚙️ Configuration (application.yml)

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
        static: us-east-1 # An arbitrary region
      # Specific configuration for SQS pointing to ElasticMQ
      sqs:
        # Points to the ElasticMQ container port
        endpoint: ${SPRING_CLOUD_AWS_SQS_ENDPOINT:http://localhost:9324}
      credentials:
        # Dummy credentials that satisfy the AWS SDK requirement
        access-key: dummy
        secret-key: dummy
cola:
  aws:
    sqs:
      producer: "cola-aws-sqs-1"
      consumer: "cola-aws-sqs-2"
```

## 🚀 Execution

```bash
make clean
make build
make bootJar
make bootRun
```