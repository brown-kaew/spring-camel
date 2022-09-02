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
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.schema.registry.client.SchemaRegistryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class AppConfig {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val versionMap = HashMap<String, Int>()

    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        return objectMapper
    }

    @Bean
    fun avroMapper(schemaRegistryClient: SchemaRegistryClient): AvroMapper {
        val avroMapper = AvroMapper.builder().build()
        avroMapper.registerKotlinModule()
        registerAvroSchema(avroMapper, schemaRegistryClient, Person::class.java)
        return avroMapper
    }

    private fun <T> registerAvroSchema(avroMapper: AvroMapper, schemaRegistryClient: SchemaRegistryClient, type: Class<T>) {
        val avroSchemaGenerator = AvroSchemaGenerator()
        avroMapper.acceptJsonFormatVisitor(type, avroSchemaGenerator)
        val schema = avroSchemaGenerator.avroSchema
        val response = schemaRegistryClient.register(type.name, "avro", schema.toString())
        log.info("Schema\n{}", schema.toString(true))
        log.info("{} : {}", response.id, response.schemaReference)
        versionMap[type.name] = response.id
    }

    @Bean
    fun writerSchemaResolver(avroMapper: AvroMapper, schemaRegistryClient: SchemaRegistryClient): SchemaResolver {
        return SchemaResolver {
            val writerClassName = Person::class.java.name
            val schema = versionMap[writerClassName]?.let { id ->
                it.message.setHeader(AppRouteBuilder.SCHEMA_ID, id)
                it.message.setHeader(AppRouteBuilder.SCHEMA_CLASS, writerClassName)
                schemaRegistryClient.fetch(id)
            }
            AvroSchema(Schema.Parser().parse(schema))
        }
    }

    @Bean
    fun readerSchemaResolver(avroMapper: AvroMapper, schemaRegistryClient: SchemaRegistryClient): SchemaResolver {
        return SchemaResolver {
            val writerId = it.message.getHeader(AppRouteBuilder.SCHEMA_ID, Int::class.java)
            val writerClassName = it.message.getHeader(AppRouteBuilder.SCHEMA_CLASS, String::class.java)
            val writerSchema = schemaRegistryClient.fetch(writerId)
            val readerSchema = versionMap[writerClassName]?.let { id -> schemaRegistryClient.fetch(id) }
            AvroSchema(Schema.Parser().parse(writerSchema))
                .withReaderSchema(AvroSchema(Schema.Parser().parse(readerSchema)))
        }
    }

}