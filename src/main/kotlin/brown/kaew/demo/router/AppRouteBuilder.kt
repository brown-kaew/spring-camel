package brown.kaew.demo.router

import brown.kaew.demo.config.RabbitMqConfig.Companion.MY_EXCHANGE
import brown.kaew.demo.model.Person
import org.apache.camel.ExchangePattern
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.dataformat.AvroLibrary
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class AppRouteBuilder : RouteBuilder() {

    companion object {
        const val PRODUCE_PERSON_ROUTE = "direct:produce.person"
        const val PRODUCE_PERSON_BACKGROUND_ROUTE = "direct:produce.person.bg"
        const val CONSUME_PERSON_ROUTE = "direct:consume.person"
        const val RABBIT_ROUTE =
            "spring-rabbitmq:brown.kaew?routingKey=brown.kaew.avro&queues=brown.kaew.avro.test&autoDeclare=true"
        const val APP_VERSION = "appVersion"
        const val MSG_CLASS = "msgClass"

        const val PRODUCE_MYQUEUE_ROUTE = "direct:produce.myqueue"
        const val PRODUCE_RABBIT_MYQUEUE_ROUTE = "spring-rabbitmq:${MY_EXCHANGE}?routingKey=my-routing-key&queues=my-queue"
        const val CONSUME_RABBIT_MYQUEUE_ROUTE = "spring-rabbitmq:${MY_EXCHANGE}?routingKey=my-routing-key&queues=my-queue"
//        const val PRODUCE_RABBIT_MYQUEUE2_ROUTE = "spring-rabbitmq:${MY_EXCHANGE}?routingKey=my-queue2-routing-key-dlq&queues=my-queue2-dlq"
        const val PRODUCE_RABBIT_MYQUEUE2_ROUTE = "spring-rabbitmq:${MY_EXCHANGE}?routingKey=my-queue2-routing-key&queues=my-queue2"
        const val CONSUME_RABBIT_MYQUEUE2_ROUTE = "spring-rabbitmq:${MY_EXCHANGE}?routingKey=my-queue2-routing-key&queues=my-queue2"
    }

    override fun configure() {

        from(PRODUCE_PERSON_BACKGROUND_ROUTE)
            .process {
                val person = it.getIn().getBody(Person::class.java)
                person.apply {
                    name = "Kaew"
                    favoriteColor = "red"
                    favoriteNumber = (Math.random() * 100).roundToInt()
                }
            }
            .process { log.info("before marshal : {}", it.getIn().body) }
            .marshal().avro(AvroLibrary.Jackson, Person::class.java, "writerSchemaResolver")
            .process { log.info("after marshal : {}", it.getIn().body) }
            .to(ExchangePattern.InOnly, RABBIT_ROUTE)

        from(RABBIT_ROUTE)
            .process { log.info("rabbitMQ consume msg size : {}", (it.getIn().body as ByteArray).size) }
            .to(CONSUME_PERSON_ROUTE)

        from(PRODUCE_PERSON_ROUTE)
            .process {
                val person = it.getIn().getBody(Person::class.java)
                person.apply {
                    name = "Kaew"
                    favoriteColor = "red"
                    favoriteNumber = (Math.random() * 100).roundToInt()
                }
            }
            .process { log.info("before marshal : {}", it.getIn().body) }
            .marshal().avro(AvroLibrary.Jackson, Person::class.java, "writerSchemaResolver")
            .process { log.info("after marshal : {}", it.getIn().body) }
            .to(CONSUME_PERSON_ROUTE)

        from(CONSUME_PERSON_ROUTE)
            .process { log.info("before unmarshal : {}", it.getIn().body) }
            .process { log.info("headers : {}", it.getIn().headers) }
            .unmarshal().avro(AvroLibrary.Jackson, Person::class.java, "readerSchemaResolver")
            .process { log.info("after unmarshal : {}", it.getIn().body) }
            .to("log:info")

        // Produce message to my-queue
        from(PRODUCE_MYQUEUE_ROUTE)
            .routeId("produce-myqueue")
            .setBody(constant("Test message for my-queue"))
            .to(PRODUCE_RABBIT_MYQUEUE_ROUTE)
            .log("Produced message to my-queue: \${body}")

        // Consume from my-queue and simulate failure to trigger DLQ
        from(CONSUME_RABBIT_MYQUEUE_ROUTE)
            .routeId("consume-myqueue")
            .log("Consuming from my-queue: \${body}")
            .process {
                val headers = it.`in`.headers
                val xDeath = headers["x-death"]
                log.info("x-death header: $xDeath")
                var deathCount = 0
                if (xDeath is List<*>) {
                    val firstDeath = xDeath.firstOrNull()
                    if (firstDeath is Map<*, *>) {
                        val count = firstDeath["count"]
                        if (count is Long) {
                            deathCount = count.toInt()
                        } else if (count is Int) {
                            deathCount = count
                        }
                    }
                }
                log.info("x-death count: $deathCount")
                if (deathCount < 2) {
                    throw RuntimeException("Simulated processing failure for my-queue DLQ test, death count: $deathCount")
                }
                log.info("Message sent to my-queue2 after $deathCount deaths: ${it.`in`.body}")
            }.to(PRODUCE_RABBIT_MYQUEUE2_ROUTE)

        from(CONSUME_RABBIT_MYQUEUE2_ROUTE)
            .routeId("consume-myqueue2")
            .log("Consuming from my-queue2: \${body}")
            .process {
                val headers = it.`in`.headers
                val xDeath = headers["x-death"]
                log.info("x-death header: $xDeath")
                var deathCount = 0
                if (xDeath is List<*>) {
                    val firstDeath = xDeath.firstOrNull()
                    if (firstDeath is Map<*, *>) {
                        val count = firstDeath["count"]
                        if (count is Long) {
                            deathCount = count.toInt()
                        } else if (count is Int) {
                            deathCount = count
                        }
                    }
                }
                log.info("x-death count: $deathCount")
                if (deathCount < 3) {
                    throw RuntimeException("Simulated processing failure for my-queue2 DLQ test, death count: $deathCount")
                }
                log.info("Message stopped after $deathCount deaths: ${it.`in`.body}")
            }

    }


}