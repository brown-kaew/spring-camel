package brown.kaew.demo.router

import brown.kaew.demo.model.Person
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.dataformat.AvroLibrary
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class AppRouteBuilder : RouteBuilder() {

    companion object {
        const val PRODUCE_PERSON_ROUTE = "direct:produce.person"
        const val CONSUME_PERSON_ROUTE = "direct:consume.person"
    }

    override fun configure() {

        from(PRODUCE_PERSON_ROUTE)
            .process { log.info("before marshal : {}", it.getIn().body) }
//            .marshal().json(JsonLibrary.Jackson, Person::class.java)
            .marshal().avro(AvroLibrary.Jackson, Person::class.java)
            .process { log.info("after marshal : {}", it.getIn().body) }
            .to(CONSUME_PERSON_ROUTE)

        from(CONSUME_PERSON_ROUTE)
            .process { log.info("before unmarshal : {}", it.getIn().body) }
//            .unmarshal().json(JsonLibrary.Jackson, Person::class.java)
            .unmarshal().avro(AvroLibrary.Jackson, Person::class.java)
            .process {
                val person = it.getIn().getBody(Person::class.java)
                person.name += "-Unmarshal"
                person.favoriteNumber = (Math.random() * 10).roundToInt()
            }
            .process { log.info("after unmarshal : {}", it.getIn().body) }
            .to("log:info")
    }


}