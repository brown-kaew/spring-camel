package brown.kaew.demo.router

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class AppRouteBuilder : RouteBuilder() {

    companion object {
        const val PRODUCE_PERSON_ROUTE = "direct:produce.person"
        const val CONSUME_PERSON_ROUTE = "direct:consume.person"
    }

    override fun configure() {
        from(PRODUCE_PERSON_ROUTE)
            .to("log:info")
    }


}