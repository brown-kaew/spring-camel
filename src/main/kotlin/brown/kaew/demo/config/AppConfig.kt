package brown.kaew.demo.config

import brown.kaew.demo.model.Person
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.avro.AvroFactory
import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator
import com.sun.istack.logging.Logger
import org.apache.camel.component.jackson.SchemaResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    val log = Logger.getLogger(AppConfig::class.java)

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @Bean
    fun avroMapper(): AvroMapper {
        val avroMapper = AvroMapper.builder(AvroFactory()).build()
        avroMapper.acceptJsonFormatVisitor(Person::class.java, AvroSchemaGenerator())
        return avroMapper
    }

    @Bean
    fun schemaResolver(avroMapper: AvroMapper): SchemaResolver {
        return SchemaResolver {
            val aClass = it.getIn().body.javaClass
            try {
//                avroMapper.schemaFor(aClass)
                avroMapper.schemaFor(Person::class.java) //Fixed type
            } catch (e: JsonMappingException) {
                throw IllegalArgumentException("No schema found", e)
            }
        }
    }

}