package brown.kaew.demo.config

import brown.kaew.demo.model.Person
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
import java.io.File

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
    fun schemaResolver(avroMapper: AvroMapper): SchemaResolver {
        return SchemaResolver {
            val writerSchema = AvroSchema(Schema.Parser().parse(File("user.avsc"))) // old writer schema
            val readerSchema = avroMapper.schemaFor(Person::class.java)
            writerSchema.withReaderSchema(readerSchema)
        }
    }

}