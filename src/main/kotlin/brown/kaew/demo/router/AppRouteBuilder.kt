package brown.kaew.demo.router

import brown.kaew.demo.model.Person
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.reflect.ReflectData
import org.apache.camel.ExchangePattern
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.dataformat.avro.AvroDataFormat
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class AppRouteBuilder() : RouteBuilder() {

    companion object {
        const val PRODUCE_PERSON_ROUTE = "direct:produce.person"
        const val PRODUCE_PERSON_BACKGROUND_ROUTE = "direct:produce.person.bg"
        const val CONSUME_PERSON_ROUTE = "direct:consume.person"
        const val RABBIT_ROUTE = "spring-rabbitmq:brown.kaew?routingKey=brown.kaew.avro&queues=brown.kaew.avro.test"
    }

    override fun configure() {
        val schema = ReflectData.get().getSchema(Person::class.java)
        log.info("Schema\n {}", schema)
        val avroDataFormat = AvroDataFormat(schema)

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
            .process {
                val p = it.`in`.body as Person
                val record = GenericData.Record(schema)
                record.put("name", p.name)
                record.put("favoriteColor", p.favoriteColor)
                record.put("favoriteNumber", p.favoriteNumber)
                record.put("favoriteFood", p.favoriteFood)
                it.`in`.body = record
            }
            .marshal(avroDataFormat)
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
            .marshal(avroDataFormat)
            .process { log.info("after marshal : {}", it.getIn().body) }
            .to(CONSUME_PERSON_ROUTE)

        from(CONSUME_PERSON_ROUTE)
            .process { log.info("before unmarshal : {}", it.getIn().body) }
            .unmarshal(avroDataFormat)
//            .process {
//                val r = it.`in`.body as GenericData.Record
//                val p = Person().apply {
//                    name = r["name"] as String
//                    favoriteColor = r["favoriteColor"] as String
//                    favoriteNumber = r["favoriteNumber"] as Int
//                    favoriteFood = r["favoriteNumber"] as String
//                }
//                it.`in`.body = p
//            }
            .process { log.info("after unmarshal : {}", it.getIn().body) }
            .to("log:info")
    }


}