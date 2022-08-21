package brown.kaew.demo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1")
class V1Controller {

    @GetMapping("hello")
    fun hello(@RequestParam name: String): String {
        return "Hello $name"
    }
}