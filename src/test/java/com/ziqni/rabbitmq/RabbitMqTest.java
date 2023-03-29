/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.rabbitmq;

import com.rabbitmq.client.*;
import com.ziqni.config.TransformerConfigLoader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RabbitMqTest {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqTest.class);

    private final RabbitMqConnection rabbitMqConnection;

    private static final String routingKey = "";
    private static final String queueName = "";

    private static final AMQP.BasicProperties basicProperties = null;

    public RabbitMqTest() {
        this.rabbitMqConnection = new RabbitMqConnection(
                TransformerConfigLoader.getRabbitMqBrokerHost(),
                TransformerConfigLoader.getRabbitMqBrokerPort(),
                TransformerConfigLoader.getRabbitMqBrokerUsername(),
                TransformerConfigLoader.getRabbitMqBrokerPassword(),
                TransformerConfigLoader.getRabbitMqBrokerVirtualHost(),
                TransformerConfigLoader.getRabbitMqBrokerSslEnabled()
        );
    }

    @Test
    void publishAndConsumeMessage() throws IOException {

        var channel = rabbitMqConnection.getChannel();
        String message = "hello world!";
        channel.basicPublish(TransformerConfigLoader.getRabbitMqBrokerExchange(), routingKey, basicProperties, message.getBytes());

        logger.info("Message published");

        channel.queueBind(queueName, TransformerConfigLoader.getRabbitMqBrokerExchange(), routingKey);

        var consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                logger.info("Received message [{}]", message);
            }
        };

        channel.basicConsume(queueName, consumer);
    }
}
