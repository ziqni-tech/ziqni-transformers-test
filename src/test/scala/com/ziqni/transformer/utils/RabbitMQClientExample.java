/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.utils;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.util.HashMap;

public class RabbitMQClientExample {

    public Channel send(String message) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        String queueName = "sisalit_stg_events";
        String routingKey = ""; // usually same as the queue name, confirm with supplier
        factory.setHost("broker-service.competitionlabs.com");
        factory.setUsername("sisal_stg_pub_client");
        factory.setPassword("");
        factory.setPort(30005);
        factory.setVirtualHost("/");
        factory.useSslProtocol();


        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            HashMap args = new HashMap<String, Object>();
            args.put("x-expires", 30 * 60 * 1000);

            channel.queueDeclare(queueName, false, false, false, args);
            channel.basicPublish("sisalit_stg_exc", routingKey, null, message.getBytes());

            return channel;
        }catch(Exception e) {
            throw e;
        }
    }

}
