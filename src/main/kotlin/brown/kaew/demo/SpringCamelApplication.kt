package brown.kaew.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringCamelApplication

fun main(args: Array<String>) {
	runApplication<SpringCamelApplication>(*args)
}
