package brown.kaew.demo.controller

import brown.kaew.demo.model.Person
import brown.kaew.demo.router.AppRouteBuilder
import org.apache.camel.ProducerTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1")
class V1Controller(
    val producerTemplate: ProducerTemplate,
) {

    @GetMapping("hello")
    fun hello(@RequestParam name: String): String {
        //http://localhost:8080/api/v1/hello?name=Kaew
        return "Hello $name"
    }

    @GetMapping("person")
    fun person(): Person {
        //http://localhost:8080/api/v1/person
        val person = Person("Kaew", "red", 9)
        return producerTemplate.requestBody(AppRouteBuilder.PRODUCE_PERSON_ROUTE, person) as Person
    }
}