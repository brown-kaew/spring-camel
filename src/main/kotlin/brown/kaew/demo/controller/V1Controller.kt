package brown.kaew.demo.controller

import brown.kaew.demo.model.Person
import brown.kaew.demo.router.AppRouteBuilder
import org.apache.camel.ProducerTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1")
class V1Controller(
    val producerTemplate: ProducerTemplate,
) {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("hello")
    fun hello(@RequestParam name: String): String {
        //http://localhost:8080/api/v1/hello?name=Kaew
        return "Hello $name"
    }

    @GetMapping("person")
    fun person(): Person {
        //http://localhost:8080/api/v1/person
        return producerTemplate.requestBody(AppRouteBuilder.PRODUCE_PERSON_ROUTE, Person()) as Person
    }

    @GetMapping("person-bg")
    fun personBg(): String {
        //http://localhost:8080/api/v1/person-bg
        producerTemplate.asyncRequestBody(AppRouteBuilder.PRODUCE_PERSON_BACKGROUND_ROUTE, Person())
        log.info("Success")
        return "Success"
    }
}