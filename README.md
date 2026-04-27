# Bulkhead Demo (Spring Boot + Resilience4j + Virtual Threads)

![Build](https://github.com/ByteEntropyCom/bulkhead-demo/actions/workflows/ci-cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Resilience4j](https://img.shields.io/badge/Resilience4j-2.2.0-orange)
![License](https://img.shields.io/badge/license-MIT-green)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)
![Issues](https://img.shields.io/github/issues/ByteEntropyCom/bulkhead-demo)


A project showcasing **Semaphore Bulkhead** and **Thread Pool Bulkhead** using Resilience4j in a Spring Boot 3.2+ application with **Java 21 Virtual Threads**.

## Features

- Semaphore Bulkhead (`@Bulkhead`) with fallback
- Thread Pool Bulkhead (`@Bulkhead(type = THREADPOOL)`) with fallback
- Full support for **Java 21 Virtual Threads**
- Configurable limits via properties / environment variables
- Integration tests that force bulkhead limits to verify fallback behavior
- Actuator + Prometheus metrics enabled
- Docker support

## Tech Stack

- Java 21
- Spring Boot 3.2.5
- Resilience4j 2.2.0
- Maven
- JUnit 5 + TestRestTemplate
- Docker (Eclipse Temurin 21 JRE Alpine)


## 2. Running Locally

** Build the project with Maven:** 

```mvn clean install```

**  Run the Spring Boot application: ** 

``` java -jar target/bulkhead-demo-0.0.1-SNAPSHOT.jar```

## 3. Docker
 
 ** Build and run with Docker: ** 

 ```
docker build -t bulkhead-demo:latest .
docker run -p 8080:8080 bulkhead-demo:latest
```

## 3. Test Endpoints

```
# Semaphore Bulkhead
curl http://localhost:8080/semaphore

# Thread Pool Bulkhead (returns CompletableFuture)
curl http://localhost:8080/threadpool
```

## 4. Configuration

**Default properties:** (`application.properties`)

```properties
spring.application.name=bulkhead-demo
server.port=8080

spring.threads.virtual.enabled=true

# Semaphore Bulkhead (serviceA)
resilience4j.bulkhead.instances.serviceA.max-concurrent-calls=5
resilience4j.bulkhead.instances.serviceA.max-wait-duration=0

# Thread Pool Bulkhead (serviceB)
resilience4j.thread-pool-bulkhead.instances.serviceB.max-thread-pool-size=3
resilience4j.thread-pool-bulkhead.instances.serviceB.core-thread-pool-size=2
resilience4j.thread-pool-bulkhead.instances.serviceB.queue-capacity=1
resilience4j.thread-pool-bulkhead.instances.serviceB.keep-alive-duration=100ms

# Metrics
management.endpoints.web.exposure.include=health
resilience4j.bulkhead.metrics.enabled=true
resilience4j.thread-pool-bulkhead.metrics.enabled=true
```


## 5. License
MIT License


