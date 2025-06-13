package brown.kaew.demo.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

    companion object {
        const val MY_EXCHANGE = "my-exchange"
    }

    @Bean
    fun myExchange(): DirectExchange {
        return DirectExchange(MY_EXCHANGE)
    }

    @Bean
    fun myQueue(): Queue {
        val args = mapOf(
            "x-dead-letter-exchange" to MY_EXCHANGE,
            "x-dead-letter-routing-key" to "my-routing-key-dlq",
        )
        return Queue("my-queue", true, false, false, args)
    }

    @Bean
    fun myQueueDlq(): Queue {
        val args = mapOf(
            "x-dead-letter-exchange" to MY_EXCHANGE,
            "x-dead-letter-routing-key" to "my-routing-key",
            "x-message-ttl" to 6000
        )
        return Queue("my-queue-dlq", true, false, false, args)
    }

    // --- my-queue2 and its DLQ ---
    @Bean
    fun myQueue2(): Queue {
        val args = mapOf(
            "x-dead-letter-exchange" to MY_EXCHANGE,
            "x-dead-letter-routing-key" to "my-queue2-routing-key-dlq",
        )
        return Queue("my-queue2", true, false, false, args)
    }

    @Bean
    fun myQueue2Dlq(): Queue {
        val args = mapOf(
            "x-dead-letter-exchange" to MY_EXCHANGE,
            "x-dead-letter-routing-key" to "my-queue2-routing-key",
            "x-message-ttl" to 60000
        )
        return Queue("my-queue2-dlq", true, false, false, args)
    }

    @Bean
    fun myQueueBinding(): Binding {
        return BindingBuilder.bind(myQueue())
            .to(myExchange())
            .with("my-routing-key")
    }

    @Bean
    fun myQueueDlqBinding(): Binding {
        return BindingBuilder.bind(myQueueDlq())
            .to(myExchange())
            .with("my-routing-key-dlq")
    }

    @Bean
    fun myQueue2Binding(): Binding {
        return BindingBuilder.bind(myQueue2())
            .to(myExchange())
            .with("my-queue2-routing-key")
    }

    @Bean
    fun myQueue2DlqBinding(): Binding {
        return BindingBuilder.bind(myQueue2Dlq())
            .to(myExchange())
            .with("my-queue2-routing-key-dlq")
    }


}