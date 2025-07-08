# 🚀 microGO – Modular Ride Sharing Microservices

Welcome to **microGO** — a clean, modular ride-sharing system inspired by Uber, built with modern backend architecture using **Java Spring Boot**, **Docker**, and (soon) **Kubernetes**.

Each component is a microservice maintained in its own Git repository, but brought together under this umbrella repo using Git submodules.

---

## 📦 Included Microservices

| Service Name         | Description                                                         |
|----------------------|---------------------------------------------------------------------|
| `ride-request`       | Handles ride creation, fare calculation, and matching               |
| `location-rider`     | Tracks and updates rider locations in real-time                     |
| `location-saver`     | Persists GPS and location data using Redis/MySQL                    |
| `gateway`            | API Gateway using Spring Cloud Gateway                              |
| `discovery`          | Service registry using Eureka for service discovery                 |
| `centralized-config` | Centralized configuration using Spring Cloud Config Server          |
| `mysql-init`         | Initializes MYSQL database before being used by other microservices |

All services are Spring Boot applications, containerized with Docker and designed for orchestration via Docker Compose or Kubernetes.

---

## 🧩 Architecture Overview

- 🧱 **Microservices**: Modular and independently deployable
- 🚏 **API Gateway**: Routes external traffic to the right service
- 🔍 **Service Discovery**: Enables services to find each other dynamically
- 📡 **Redis**: Used for fast location tracking (Geo-indexing)
- 📦 **MySQL**: Stores ride and user data
- 📊 **(Coming soon)**: Monitoring with Grafana and Prometheus
- ☸️ **(Coming soon)**: Deployment with Kubernetes & Helm
---

## 🔧 Getting Started (Docker Compose)

> ⚠️ Before running, make sure you have cloned all submodules

```bash
git clone --recurse-submodules https://github.com/your-username/microGO.git
cd microGO
```

Start all services locally with:
```docker compose up --build```

If you didn't clone with --recurse-submodules, run:
```git submodule update --init --recursive```


## 🧪 Project Goals
✅ Build a realistic, production-style backend project

✅ Use microservices, service discovery, and message queues

✅ Deploy with Docker & Kubernetes

✅ Write clear technical articles documenting each step

## 📚 Learn More
I’m documenting the journey through technical blog posts on:

- [Medium](https://medium.com/@farhanii.henda)

- Dev.to (soon)

Follow along for insights on:

- Building scalable Spring Boot microservices

- Deploying to Kubernetes

- Using Redis Geo features for ride tracking

- Setting up monitoring with Grafana

## 💖 Credits
Developed by @[hendafarhani](https://github.com/hendafarhani) with love, coffee, and a passion for clean architecture.

