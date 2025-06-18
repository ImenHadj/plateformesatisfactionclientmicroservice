package satisfactionclient.Enquete_service.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_REQUEST_QUEUE = "user.request.queue";
    public static final String USER_RESPONSE_QUEUE = "user.response.queue";
    public static final String IA_REQUEST_QUEUE = "ia.request.queue";
    public static final String IA_RESPONSE_QUEUE = "ia.response.queue";


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
        return new Queue(IA_REQUEST_QUEUE, true); // ✅ durable = true
    }

    @Bean
    public Queue iaResponseQueue() {
        return new Queue(IA_RESPONSE_QUEUE, true); // ✅ aussi pour la réponse
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*"); // ✅ Autorise tous les packages pour éviter l'erreur de type
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter()); // ✅ AJOUTÉ ICI
        rabbitTemplate.setReplyTimeout(15000); // 15s timeout
        return rabbitTemplate;
    }




}