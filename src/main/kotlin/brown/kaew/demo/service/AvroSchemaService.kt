package brown.kaew.demo.service

import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.AvroSchema
import org.apache.avro.Schema
import org.redisson.api.RedissonClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class AvroSchemaService(
    private val redissonClient: RedissonClient,
    private val avroMapper: AvroMapper,
    @Value("\${info.build.version}")
    val appVersion: String
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    private fun <T> getSchema(version: String, type: Class<T>): AvroSchema {
        val key = "avro:" + type.name + ":" + version
        log.info("getSchema key : {}", key)
        val bucket = redissonClient.getBucket<String>(key)
        var schema = bucket.get()

        if (schema == null && version == appVersion) {
            //only manage version of this application
            val avroSchema = avroMapper.schemaFor(type)
            schema = avroSchema.avroSchema.toString()
            if (bucket.trySet(schema, 1, TimeUnit.DAYS)) { //store to redis
                log.info("Store schema : {}", key)
            }
            return avroSchema
        }

        return AvroSchema(Schema.Parser().parse(schema))
    }

    @Cacheable("avroSchema", key = "'writer:' + #type")
    fun <T> getWriterSchema(type: Class<T>): AvroSchema {
        return getSchema(appVersion, type)
    }

    @Cacheable("avroSchema", key = "'reader:' + #type + ':' + #writerVersion")
    fun <T> getReaderSchema(writerVersion: String, type: Class<T>): AvroSchema {
        return if (writerVersion == appVersion) {
            getSchema(writerVersion, type)
        } else {
            val writer = getSchema(writerVersion, type)
            val reader = getSchema(appVersion, type)
            writer.withReaderSchema(reader)
        }
    }

}