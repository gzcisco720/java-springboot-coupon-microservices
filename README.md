## Coupon microservice project

### Technologies 

- Maven
- Springboot
- SpringCloud
- Mysql
- Redis
- Kafka
- Docker

### Modules

##### 1: Eureka Server (coupon-eureka)
##### 2: Zuul Gateway (coupon-gateway)
##### 3: Coupon services

- Common module
- Coupon Distribution service
- Coupon Template service
- Coupon Settlement service

### Use case

1: Administrator can generate coupon template via template service

2: User can pick a coupon via distribution service

3: User can use coupon via settlement service

### Run

- `docker-compose up -d` set up persist environment
- start coupon-eureka, coupon-gateway services
- start coupon-template, coupon-settlement, coupon-distribution services

### UIs

- Redis-Ui: http://localhost:7843/
- Eureka-Server: http://localhost:8000/
- Kafka-Ui: http://localhost:9021/

