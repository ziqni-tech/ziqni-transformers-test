/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */
package com.ziqni.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransformerConfigLoader {

    public static String DefaultConfigFileName = "application.properties";
    private static Map<String, String> cache;
    private static final Logger logger = LoggerFactory.getLogger(TransformerConfigLoader.class);

    private static Boolean loaded = false;

    private static String rabbitMqBrokerHost;
    private static Integer rabbitMqBrokerPort;
    private static String rabbitMqBrokerUsername;
    private static String rabbitMqBrokerPassword;
    private static String rabbitMqBrokerVirtualHost;
    private static Boolean rabbitMqBrokerSslEnabled;
    private static String rabbitMqBrokerExchange;

    private static void load() {
        if(loaded)
            return;

        loadFromFile();
        rabbitMqBrokerHost = cache.get("rabbitmq.broker.host");
        rabbitMqBrokerPort = Integer.valueOf(cache.get("rabbitmq.broker.port"));
        rabbitMqBrokerUsername = cache.get("rabbitmq.broker.username");
        rabbitMqBrokerPassword = cache.get("rabbitmq.broker.password");
        rabbitMqBrokerVirtualHost = cache.get("rabbitmq.broker.virtualhost");
        rabbitMqBrokerSslEnabled = Boolean.valueOf(cache.get("rabbitmq.broker.sslEnabled"));
        rabbitMqBrokerExchange = cache.get("rabbitmq.broker.exchange");

        loaded = true;
    }

    public static String getRabbitMqBrokerHost() {
        load();
        return rabbitMqBrokerHost;
    }

    public static Integer getRabbitMqBrokerPort() {
        load();
        return rabbitMqBrokerPort;
    }

    public static String getRabbitMqBrokerUsername() {
        load();
        return rabbitMqBrokerUsername;
    }

    public static String getRabbitMqBrokerPassword() {
        load();
        return rabbitMqBrokerPassword;
    }

    public static String getRabbitMqBrokerVirtualHost() {
        load();
        return rabbitMqBrokerVirtualHost;
    }

    public static Boolean getRabbitMqBrokerSslEnabled() {
        load();
        return rabbitMqBrokerSslEnabled;
    }

    public static String getRabbitMqBrokerExchange() {
        load();
        return rabbitMqBrokerExchange;
    }

    /*
     * Load config from file
     */
    private static void loadFromFile() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName(DefaultConfigFileName));

        logger.debug("Loaded config file [{}] from location [{}]", DefaultConfigFileName, builder.getFileHandler().getPath());

        try {
            Configuration config = builder.getConfiguration();
            var it = config.getKeys();

            if(TransformerConfigLoader.cache == null)
                TransformerConfigLoader.cache = new HashMap<>();

            while (it.hasNext()){
                var key = it.next();
                var configValue = config.getString(key);
                TransformerConfigLoader.cache.put(key, configValue);
                logger.debug("Overwriting loaded parameter [{}] with value [{}] from file [{}]", key, configValue, DefaultConfigFileName);
            }
        }
        catch(org.apache.commons.configuration2.ex.ConfigurationException cex) {
            logger.error("Loading of the configuration file failed");
            throw new RuntimeException(cex);
        }

        assert cache != null;
        assert cache.size() > 0;
    }

}
