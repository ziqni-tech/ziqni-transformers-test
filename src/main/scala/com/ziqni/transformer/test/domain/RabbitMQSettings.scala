/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.test.domain

import java.util

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

case class RabbitMQSettings(
							   host: String,
							   port: Int,
							   username: String,
							   password: String,
							   vHost: String,
							   queueName: String,
							   useSSL: Boolean,
							   exchange: Option[String],
							   routingKey: Option[String]
						   ){

	var connection: Option[Connection] = None

	var channel: Option[Channel] = None

	def shutdown(): Unit = connection.foreach(cn => channel.foreach( ch => RabbitMQ.shutdownMQConnection(ch, cn)))
}

object RabbitMQ {

	def getMQConnection(rabbitMQSettings: RabbitMQSettings):RabbitMQSettings  = {

		val factory: ConnectionFactory = new ConnectionFactory()
		factory.setHost(rabbitMQSettings.host)
		factory.setUsername(rabbitMQSettings.username)
		factory.setPassword(rabbitMQSettings.password)
		factory.setPort(rabbitMQSettings.port)
		factory.setVirtualHost(rabbitMQSettings.vHost)
		if(rabbitMQSettings.useSSL)
			factory.useSslProtocol()

		val connection: Connection = factory.newConnection()
		val channel: Channel = connection.createChannel()

		lazy val connectionArguments: util.HashMap[String, Object] = {
			lazy val expiryTime: Integer = 30 * 60 * 1000
			lazy val args = new util.HashMap[String, Object]()
			args.put("x-expires", expiryTime) /** to auto-delete queue if it is not used for 30 minutes **/
			args
		}

		channel.queueDeclare(rabbitMQSettings.queueName, false,false,false,connectionArguments)

		rabbitMQSettings.channel = Option(channel)
		rabbitMQSettings.connection = Option(connection)

		rabbitMQSettings
	}

	def shutdownMQConnection(channel: Channel, connection: Connection): Unit = {
		channel.close()
		connection.close()
	}
}