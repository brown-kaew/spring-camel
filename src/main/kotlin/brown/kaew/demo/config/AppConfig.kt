package brown.kaew.demo.config

import brown.kaew.demo.model.Person
import brown.kaew.demo.router.AppRouteBuilder
import brown.kaew.demo.service.AvroSchemaService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.camel.component.jackson.SchemaResolver
import org.ehcache.config.CacheConfiguration
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.core.config.DefaultConfiguration
import org.ehcache.jsr107.EhcacheCachingProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import javax.cache.Caching

@Configuration
@EnableCaching
class AppConfig(
    @Value("\${info.build.version:none}")
    private var version: String
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun ehcacheManager(): CacheManager {
        val cacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(
                Any::class.java,
                Any::class.java, ResourcePoolsBuilder.heap(1000)
            )
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10L)))
            .build()
        val cacheMap: MutableMap<String, CacheConfiguration<*, *>> = HashMap()
        cacheMap["avroSchema"] = cacheConfiguration

        val ehcacheCachingProvider =
            Caching.getCachingProvider(EhcacheCachingProvider::class.java.name) as EhcacheCachingProvider
        val defaultConfiguration = DefaultConfiguration(cacheMap, ehcacheCachingProvider.defaultClassLoader)
        val cacheManager =
            ehcacheCachingProvider.getCacheManager(ehcacheCachingProvider.defaultURI, defaultConfiguration)
        return JCacheCacheManager(cacheManager)
    }

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
    fun writerSchemaResolver(avroSchemaService: AvroSchemaService): SchemaResolver {
        return SchemaResolver {
            it.message.setHeader(AppRouteBuilder.APP_VERSION, version)
            avroSchemaService.getSchema(version, Person::class.java)
        }
    }

    @Bean
    fun readerSchemaResolver(avroSchemaService: AvroSchemaService): SchemaResolver {
        return SchemaResolver {
            val writerVersion = it.message.getHeader(AppRouteBuilder.APP_VERSION, String::class.java)
            val writerSchema = avroSchemaService.getSchema(writerVersion, Person::class.java) // old writer schema
            val readerSchema = avroSchemaService.getSchema(version, Person::class.java)
            writerSchema.withReaderSchema(readerSchema)
        }
    }

}