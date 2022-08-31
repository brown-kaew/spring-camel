package brown.kaew.demo.config

import brown.kaew.demo.model.Person
import brown.kaew.demo.router.AppRouteBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.AvroSchema
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.avro.Schema
import org.apache.camel.component.jackson.SchemaResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        return objectMapper
    }

    @Bean
    fun avroMapper(): AvroMapper {
        val avroMapper = AvroMapper.builder().build()
        val avroSchemaGenerator = AvroSchemaGenerator()
        avroMapper.registerKotlinModule()
        avroMapper.acceptJsonFormatVisitor(Person::class.java, avroSchemaGenerator)
        log.info("Schema\n{}", avroSchemaGenerator.avroSchema.toString(true))
        return avroMapper
    }

    @Bean
    fun writerSchemaResolver(avroMapper: AvroMapper): SchemaResolver {
        return SchemaResolver {
            val writerSchema = avroMapper.schemaFor(Person::class.java)
            it.message.setHeader(AppRouteBuilder.AVRO_SCHEMA, writerSchema.avroSchema.toString())
            writerSchema
        }
    }

    @Bean
    fun readerSchemaResolver(avroMapper: AvroMapper): SchemaResolver {
        return SchemaResolver {
            val schema = it.message.getHeader(AppRouteBuilder.AVRO_SCHEMA, String::class.java)
            val writerSchema = AvroSchema(Schema.Parser().parse(schema)) // old writer schema
            val readerSchema = avroMapper.schemaFor(Person::class.java)
            writerSchema.withReaderSchema(readerSchema)
        }
    }

}