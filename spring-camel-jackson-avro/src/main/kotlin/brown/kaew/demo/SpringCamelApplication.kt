package brown.kaew.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.schema.registry.client.EnableSchemaRegistryClient

@SpringBootApplication
@EnableSchemaRegistryClient
class SpringCamelApplication

fun main(args: Array<String>) {
	runApplication<SpringCamelApplication>(*args)
}
