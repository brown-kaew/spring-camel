# spring-camel

## prerequisite
- start rabbitMQ
- start redis

direct route
http://localhost:8080/api/v1/person

rabbitMq route
http://localhost:8080/api/v1/person-bg


## Retryable route with dead letter queue
http://localhost:8080/api/v1/produce-myqueue?message=hello