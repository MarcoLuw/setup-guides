# MSK Spring Demo

A simple Spring Boot 3.x application that interacts with AWS MSK (Managed Streaming for Apache Kafka).

## Project Structure

```
msk-spring-demo/
├── pom.xml
├── .env.example        # Template for environment variables
├── .env                # Your local config (git-ignored)
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/example/mskdemo/
        │       ├── MskSpringDemoApplication.java
        │       ├── controller/
        │       │   └── KafkaController.java
        │       └── service/
        │           ├── KafkaConsumerService.java
        │           └── KafkaProducerService.java
        └── resources/
            └── application.yml
```

## Configuration

### Step 1: Create your .env file

```bash
cp .env.example .env
```

### Step 2: Edit .env with your settings

```properties
# Choose profile: plaintext, sasl-scram, or iam
SPRING_PROFILES_ACTIVE=plaintext

# Kafka bootstrap servers
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Topic name
KAFKA_TOPIC_NAME=demo-topic

# For SASL/SCRAM authentication
MSK_USERNAME=your-username
MSK_PASSWORD=your-password

# For IAM authentication
AWS_REGION=us-east-1
```

## Run Instructions

### 1. Local Development (PLAINTEXT)

Create `.env`:
```properties
SPRING_PROFILES_ACTIVE=plaintext
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_NAME=demo-topic
```

Start a local Kafka instance (using Docker):
```bash
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_CFG_NODE_ID=0 \
  -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@localhost:9093 \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  bitnami/kafka:latest
```

Run the application:
```bash
cd msk-spring-demo
./mvnw spring-boot:run
```

### 2. Connect to MSK with SASL/SCRAM

Create `.env`:
```properties
SPRING_PROFILES_ACTIVE=sasl-scram
KAFKA_BOOTSTRAP_SERVERS=b-1.mycluster.xxx.kafka.us-east-1.amazonaws.com:9096,b-2.mycluster.xxx.kafka.us-east-1.amazonaws.com:9096
KAFKA_TOPIC_NAME=demo-topic
MSK_USERNAME=your-username
MSK_PASSWORD=your-password
```

Run the application:
```bash
./mvnw spring-boot:run
```

### 3. Connect to MSK with IAM Authentication

Create `.env`:
```properties
SPRING_PROFILES_ACTIVE=iam
KAFKA_BOOTSTRAP_SERVERS=b-1.mycluster.xxx.kafka.us-east-1.amazonaws.com:9098,b-2.mycluster.xxx.kafka.us-east-1.amazonaws.com:9098
KAFKA_TOPIC_NAME=demo-topic
AWS_REGION=us-east-1
```

Ensure AWS credentials are available via:
- `~/.aws/credentials` file
- IAM instance profile (EC2/ECS)
- Or add to `.env`:
  ```properties
  AWS_ACCESS_KEY_ID=your-access-key
  AWS_SECRET_ACCESS_KEY=your-secret-key
  ```

Run the application:
```bash
./mvnw spring-boot:run
```

## API Usage

### Publish a Message (Producer)

```bash
curl -X POST http://localhost:8080/api/kafka/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "hello"}'
```

Response:
```json
{
  "status": "success",
  "message": "Message sent to Kafka"
}
```

### Consume Messages (Consumer)

```bash
# Get up to 10 messages (default)
curl http://localhost:8080/api/kafka/consume

# Get up to 5 messages
curl http://localhost:8080/api/kafka/consume?limit=5
```

Response:
```json
{
  "count": 2,
  "messages": [
    {"offset": 0, "partition": 0, "value": "hello"},
    {"offset": 1, "partition": 0, "value": "world"}
  ]
}
```

## Create Topic Using AWS CLI

### Get MSK Cluster Info

```bash
aws kafka get-bootstrap-brokers --cluster-arn <cluster-arn>
aws kafka describe-cluster --cluster-arn <cluster-arn>
```

### Create Topic

```bash
kafka-topics.sh --create \
  --topic demo-topic \
  --partitions 3 \
  --replication-factor 2 \
  --bootstrap-server <bootstrap-servers>
```

### For IAM Authentication

Create `client.properties`:
```properties
security.protocol=SASL_SSL
sasl.mechanism=AWS_MSK_IAM
sasl.jaas.config=software.amazon.msk.auth.iam.IAMLoginModule required;
sasl.client.callback.handler.class=software.amazon.msk.auth.iam.IAMClientCallbackHandler
```

Then:
```bash
kafka-topics.sh --create \
  --topic demo-topic \
  --partitions 3 \
  --replication-factor 2 \
  --bootstrap-server <bootstrap-servers> \
  --command-config client.properties
```

## MSK Port Reference

| Authentication Method | Port |
|-----------------------|------|
| PLAINTEXT             | 9092 |
| TLS                   | 9094 |
| SASL/SCRAM            | 9096 |
| IAM                   | 9098 |

## Build for Production

```bash
./mvnw clean package -DskipTests
java -jar target/msk-spring-demo-1.0.0.jar
```

## Dependencies

- Spring Boot 3.2.0
- Spring Kafka
- AWS MSK IAM Auth 2.0.3
- Lombok
- Java 17+
