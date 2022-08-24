package brown.kaew.demo.config

import brown.kaew.demo.model.Person
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper
import com.fasterxml.jackson.dataformat.protobuf.schemagen.ProtobufSchemaGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
    fun protobufMapper(): ProtobufMapper {
        val protobufMapper = ProtobufMapper.builder().build()
        val protobufSchemaGenerator = ProtobufSchemaGenerator()
        protobufMapper.registerKotlinModule()
        protobufMapper.acceptJsonFormatVisitor(Person::class.java, protobufSchemaGenerator)
        protobufMapper.enable(JsonParser.Feature.IGNORE_UNDEFINED)
        log.info("Schema\n {}", protobufSchemaGenerator.generatedSchema)
        return protobufMapper
    }

    @Bean
    fun schemaResolver(protobufMapper: ProtobufMapper): SchemaResolver {
        return SchemaResolver {
            protobufMapper.generateSchemaFor(Person::class.java)
        }
    }

}