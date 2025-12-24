# Ligo Chat App

**Ligo Chat App** is a scalable, real-time messaging application designed for high-throughput communication and instant language translation. Built with Java and Kafka, it is cloud-native and Kubernetes-ready, could be deployed on Amazon EKS or any k8s cluster with an Ingress controller for robust traffic management.

---

## 🚀 Features

- **Real-Time Chat**: Reliable, low-latency messaging between users.
- **Auto-Translation**: Seamless message translation between multiple languages.
- **Kafka Integration**: Decoupled messaging system using Apache Kafka.
- **Microservices Architecture**: Modular, maintainable, and scalable components.
- **Kubernetes Native**: Deployed on EKS with robust ingress routing.
- **DevOps Leveraging**: Enhance deployment with CICD using Jenkins pipeline.

---

## 📦 Tech Stack

- **Java** – Core backend services (Chat, Translate)
- **Apache Kafka** – Message broker and event streaming
- **Amazon EKS** – Kubernetes orchestration
- **Ingress Controller** – Load balancing and routing
- **Docker** – Containerization
- **Jenkins** - CICD pipeline

---

## 🏗️ Architecture

> _Insert architecture image here when available._

![Architecture Diagram](path/to/your/image.png)

---

## 📁 Project Structure

```bash
ligo-chat-app/
├── chat-service/          # Handles real-time messaging
├── translate-service/     # Translates incoming messages
├── kafka-config/          # Kafka producer/consumer config
├── k8s-manifests/         # Kubernetes deployment files
└── README.md              # You're here!
