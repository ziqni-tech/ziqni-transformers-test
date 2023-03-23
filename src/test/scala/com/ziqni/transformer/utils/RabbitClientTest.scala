/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.utils

import com.rabbitmq.client.{CancelCallback, DeliverCallback}
import com.ziqni.transformer.SampleTransformer
import com.ziqni.transformer.domain.{RabbitMQ, RabbitMQSettings}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RabbitClientTest extends AnyFunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach with BeforeAndAfterAll {

	private lazy val transformer = new SampleTransformer
	private lazy val api = new ZiqniApiTest

	/**
	  * Edit this value in order to do integration testing.
	  */
	private val enableTransformer = true

	describe("Test rabbitmq client connection") {

		val rabbitMqSettings: RabbitMQSettings = RabbitMQSettings(
			host = "broker-service.competitionlabs.com",        // Example: "broker.example.com"
			username = "sisal_stg_pub_client",                 // Example: "some_user"
			password = "",                         // Example: "asdasd"
			queueName = "sisalit_stg_events",                    // Example: "some_queue_name"
			vHost = "/",
			port = 30005,
			useSSL = true,
			exchange = Option("sisalit_stg_exc"),
			routingKey = Option("")
		)

		it("should receive a message from the queue") {

			assert(rabbitMqSettings.host.nonEmpty, "Host cannot be empty")
			assert(rabbitMqSettings.username.nonEmpty, "Username cannot be empty")
			assert(rabbitMqSettings.password.nonEmpty, "Password cannot be empty")
			assert(rabbitMqSettings.queueName.nonEmpty, "Queue name cannot be empty")

			val mqSettingsInstance = RabbitMQ.getMQConnection(rabbitMqSettings)
			val channel = mqSettingsInstance.channel.get

			Then("Preparing event for posting")
			for( a <- 1 to 5){
//				val message = SampleTransformerTest.Transaction_Msq_09082021(a, 100)
				val message = s"test [${a}]"
				//			val message = SampleTransformerTest.Transaction_Msq_09082021(100, 100)
				channel.basicPublish(rabbitMqSettings.exchange.getOrElse(throw new RuntimeException("exchange not provided")), rabbitMqSettings.routingKey.getOrElse(throw new RuntimeException("routingkey not provided")), null, message.getBytes)

				System.out.println("")
				System.out.println(" [x] Sent '" + message + "'")
			}


			println(s"${DateTime.now} [Queue Channel is] " + channel)
			channel.close()


		}

		it("should receive a message from the queue") {

			val mqSettingsInstance = RabbitMQ.getMQConnection(rabbitMqSettings)
			val channel = mqSettingsInstance.channel.get

			val callback: DeliverCallback = (consumerTag, delivery) => {
				// we get the message body as a String
				val message = new String(delivery.getBody, "UTF-8")
				println(s"Received $message with tag $consumerTag")
			}

			// this is called when the consumption is canceled in
			// an abnormal way (i.e., the queue is deleted)
			val cancel: CancelCallback = consumerTag => {}

			channel.basicConsume(rabbitMqSettings.queueName, callback, cancel)


		}
	}

}
