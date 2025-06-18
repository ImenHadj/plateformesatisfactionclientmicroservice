package satisfactionclient.user_service.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_REQUEST_QUEUE = "user.request.queue";
    public static final String USER_RESPONSE_QUEUE = "user.response.queue";
    public static final String IA_REQUEST_QUEUE = "ia.request.queue";
    public static final String IA_RESPONSE_QUEUE = "ia.response.queue";
    public static final String RECLAMATION_REQUEST_QUEUE = "reclamation.request.queue";
    public static final String RECLAMATION_RESPONSE_QUEUE = "reclamation.response.queue";
    @Bean
    public Queue userRequestQueue() {
        return new Queue(USER_REQUEST_QUEUE);
    }

    @Bean
    public Queue userResponseQueue() {
        return new Queue(USER_RESPONSE_QUEUE);
    }

    @Bean
    public Queue iaRequestQueue() {
        return new Queue(IA_REQUEST_QUEUE);
    }

    @Bean
    public Queue iaResponseQueue() {
        return new Queue(IA_RESPONSE_QUEUE);
    }
    @Bean
    public Queue reclamationRequestQueue() {
        return new Queue(RECLAMATION_REQUEST_QUEUE);
    }

    @Bean
    public Queue reclamationResponseQueue() {
        return new Queue(RECLAMATION_RESPONSE_QUEUE);
    }
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter()); // 👈 Ajout essentiel
        return template;
    }
}