package brown.kaew.demo.service

import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.AvroSchema
import org.apache.avro.Schema
import org.redisson.api.RedissonClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AvroSchemaService(
    private val redissonClient: RedissonClient,
    private val avroMapper: AvroMapper
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Cacheable("avroSchema", key = "#type.name + ':' + #version")
    fun <T> getSchema(version: String, type: Class<T>): AvroSchema {
        val key = type.name + ":" + version
        log.info("getSchema key : {}", key)
        val bucket = redissonClient.getBucket<String>(key)
        var schema = bucket.get()

        if (schema == null) {
            val avroSchema = avroMapper.schemaFor(type)
            schema = avroSchema.avroSchema.toString()
            bucket.set(schema) //store to redis
            log.info("Store schema : {}", key)
            return avroSchema
        }

        return AvroSchema(Schema.Parser().parse(schema))
    }

}