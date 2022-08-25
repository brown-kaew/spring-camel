package brown.kaew.demo.router

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
        const val RABBIT_ROUTE = "spring-rabbitmq:brown.kaew?routingKey=brown.kaew.avro&queues=brown.kaew.avro.test"
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
            .marshal().avro(AvroLibrary.Jackson, Person::class.java)
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
            .marshal().avro(AvroLibrary.Jackson, Person::class.java)
            .process { log.info("after marshal : {}", it.getIn().body) }
            .to(CONSUME_PERSON_ROUTE)

        from(CONSUME_PERSON_ROUTE)
            .process { log.info("before unmarshal : {}", it.getIn().body) }
            .unmarshal().avro(AvroLibrary.Jackson, Person::class.java)
            .process { log.info("after unmarshal : {}", it.getIn().body) }
            .to("log:info")
    }


}