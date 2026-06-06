package com.foodhub.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE = "foodhub.exchange";
    public static final String DLX = "foodhub.dlx";

    // Queues
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_ACCEPTED_QUEUE = "order.accepted.queue";
    public static final String ORDER_STATUS_QUEUE = "order.status.queue";
    public static final String RESTAURANT_RATING_QUEUE = "restaurant.rating.queue";

    // Routing keys
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_ACCEPTED_KEY = "order.accepted";
    public static final String ORDER_STATUS_KEY = "order.status.updated";
    public static final String RESTAURANT_RATED_KEY = "restaurant.rated";

    @Bean
    public TopicExchange foodhubExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", "order.created.dlq")
            .build();
    }

    @Bean
    public Queue orderAcceptedQueue() {
        return QueueBuilder.durable(ORDER_ACCEPTED_QUEUE).build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE).build();
    }

    @Bean
    public Queue restaurantRatingQueue() {
        return QueueBuilder.durable(RESTAURANT_RATING_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue()).to(foodhubExchange()).with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding orderAcceptedBinding() {
        return BindingBuilder.bind(orderAcceptedQueue()).to(foodhubExchange()).with(ORDER_ACCEPTED_KEY);
    }

    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder.bind(orderStatusQueue()).to(foodhubExchange()).with(ORDER_STATUS_KEY);
    }

    @Bean
    public Binding restaurantRatingBinding() {
        return BindingBuilder.bind(restaurantRatingQueue()).to(foodhubExchange()).with(RESTAURANT_RATED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

