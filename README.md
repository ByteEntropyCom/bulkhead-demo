# Bulkhead Demo (Spring Boot + Resilience4j + Virtual Threads)

![Build](https://github.com/ByteEntropyCom/bulkhead-demo/actions/workflows/ci-cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Resilience4j](https://img.shields.io/badge/Resilience4j-2.2.0-orange)
![License](https://img.shields.io/badge/license-MIT-green)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)
![Issues](https://img.shields.io/github/issues/ByteEntropyCom/bulkhead-demo)
![Forks](https://img.shields.io/github/forks/ByteEntropyCom/bulkhead-demo)
![Stars](https://img.shields.io/github/stars/ByteEntropyCom/bulkhead-demo)

This project demonstrates how to implement the **Bulkhead pattern** using **Resilience4j** in a **Spring Boot 3** application with **Java 21 Virtual Threads**.

It showcases two types of bulkheads:

* **Semaphore Bulkhead** → Limits concurrent calls
* **Thread Pool Bulkhead** → Isolates execution using a dedicated thread pool

---

## 🚀 Tech Stack

* Java 21 (Virtual Threads enabled)
* Spring Boot 3.2.5
* Resilience4j 2.2.0
* Maven

---

## 📌 What is Bulkhead Pattern?

The Bulkhead pattern isolates different parts of a system to prevent cascading failures.

👉 If one service becomes overloaded, it won’t bring down the entire system.

---

## ⚙️ Configuration

### Application Properties

```properties
spring.application.name=bulkhead-demo
server.port=8080

# Enable Virtual Threads (Java 21)
spring.threads.virtual.enabled=true

# Semaphore Bulkhead
resilience4j.bulkhead.instances.serviceA.max-concurrent-calls=5
resilience4j.bulkhead.instances.serviceA.max-wait-duration=0

# Thread Pool Bulkhead
resilience4j.thread-pool-bulkhead.instances.serviceB.max-thread-pool-size=3
resilience4j.thread-pool-bulkhead.instances.serviceB.core-thread-pool-size=2
resilience4j.thread-pool-bulkhead.instances.serviceB.queue-capacity=1
resilience4j.thread-pool-bulkhead.instances.serviceB.keep-alive-duration=100ms
```

---

## 🧠 Implementation Overview

### 1. Semaphore Bulkhead

```java
@Bulkhead(name = "serviceA", fallbackMethod = "fallbackSemaphore")
public String slowSemaphoreCall()
```

* Allows **max 5 concurrent calls**
* Rejects extra requests immediately
* Uses **virtual threads**

---

### 2. Thread Pool Bulkhead

```java
@Bulkhead(
    name = "serviceB",
    type = Bulkhead.Type.THREADPOOL,
    fallbackMethod = "fallbackThreadPool"
)
public CompletableFuture<String> isolatedThreadCall()
```

* Uses a **dedicated thread pool**
* Prevents blocking main request threads
* Has:

  * 2 core threads
  * 3 max threads
  * Queue size = 1

---

## 📡 API Endpoints

### 1. Semaphore Endpoint

```
GET /semaphore
```

✅ Success Response:

```
Semaphore Success By Virtual Thread: true
```

❌ When overloaded:

```
Fallback: Semaphore limit reached!
```

---

### 2. Thread Pool Endpoint

```
GET /threadpool
```

✅ Success Response:

```
Thread Pool Success on: pool-X-thread-Y
```

❌ When overloaded:

```
Fallback: Thread pool saturated!
```

---

## 🧪 How to Test

### Using curl

#### Test Semaphore Bulkhead

```bash
for i in {1..10}; do curl http://localhost:8080/semaphore & done
```

👉 Only 5 requests succeed at a time
👉 Others immediately trigger fallback

---

#### Test Thread Pool Bulkhead

```bash
for i in {1..10}; do curl http://localhost:8080/threadpool & done
```

👉 Limited threads + queue
👉 Excess requests hit fallback

---

### Using Postman / JMeter

* Send concurrent requests (10+)
* Observe:

  * Throughput control
  * Fallback behavior

---

## 🔍 Key Observations

* **Virtual Threads + Semaphore Bulkhead**

  * Lightweight concurrency
  * Efficient handling of blocking calls

* **Thread Pool Bulkhead**

  * Strong isolation
  * Prevents thread starvation

---

## 📁 Project Structure

```
src/main/java/com/byteentropy/bulkhead_demo
│
├── BulkheadDemoApplication.java
├── controller
│   └── DemoController.java
└── service
    └── ExternalApiService.java
```

---

## 💡 When to Use What?

| Scenario                        | Use                         |
| ------------------------------- | --------------------------- |
| Lightweight concurrency control | Semaphore Bulkhead          |
| Isolating slow/remote calls     | Thread Pool Bulkhead        |
| Prevent blocking main threads   | Thread Pool Bulkhead        |
| High scalability (Java 21)      | Semaphore + Virtual Threads |

---

## ▶️ Run the Project

```bash
mvn spring-boot:run
```

Then open:

* http://localhost:8080/semaphore
* http://localhost:8080/threadpool

---

## 📊 Monitoring & Observability
This project is "production-ready" with built-in monitoring via Spring Boot Actuator and Micrometer. You can track the health and performance of your bulkheads in real-time.

### 1. Accessing Metrics

Once the application is running, you can view the raw Prometheus metrics at:
http://localhost:8080/actuator/prometheus

### 2. Key Bulkhead Metrics to Watch

Search (Ctrl+F) the prometheus page for bulkhead to find these critical indicators:

Metric Name	Description
resilience4j_bulkhead_concurrent_calls	Number of requests currently occupying the bulkhead.
resilience4j_bulkhead_available_concurrent_calls	Remaining capacity before the bulkhead starts rejecting requests.
resilience4j_bulkhead_not_permitted_calls_total	Critical: Total count of requests rejected by the bulkhead (triggered fallbacks).

---

## 📌 Summary

This project demonstrates:

* How to use **Resilience4j Bulkhead**
* Difference between **Semaphore vs Thread Pool isolation**
* Benefits of **Java 21 Virtual Threads**
* How to protect services from overload

---

## 🧾 License

This project is open-source and free to use.

---

## 🙌 Contribution

Feel free to fork, improve, and submit PRs!
