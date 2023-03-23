/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class RabbitMqConnection {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqConnection.class);

    private final String host;

    private final Integer port;

    private final String username;

    private final String password;

    private final String virtualHost;

    private final Boolean useSsl;

    private Connection connection;

    private Channel channel;

    public RabbitMqConnection(String host, Integer port, String username, String password,
                              String virtualHost, Boolean useSsl) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.virtualHost = virtualHost;
        this.useSsl = useSsl;
    }

    private void initConnection() {
        try {
            var factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setPort(port);
            factory.setVirtualHost(virtualHost);

            if (useSsl)
                factory.useSslProtocol();

            connection = factory.newConnection();
            channel = connection.createChannel();

        } catch (NoSuchAlgorithmException | KeyManagementException | IOException | TimeoutException e) {
            logger.error("Exception occurred during connection initialisation.", e);
        }
    }

    public Connection getConnection() {
        if (connection == null)
            initConnection();
        return connection;
    }

    public Channel getChannel() {
        if(channel == null)
            initConnection();
        return channel;
    }
}
