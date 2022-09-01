package brown.kaew.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.schema.registry.EnableSchemaRegistryServer

@SpringBootApplication
@EnableSchemaRegistryServer
class SpringSchemaRegisterApplication

fun main(args: Array<String>) {
	runApplication<SpringSchemaRegisterApplication>(*args)
}
