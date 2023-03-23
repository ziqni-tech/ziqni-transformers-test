/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.utils

import com.ziqni.transformer.SampleTransformer
import java.util
import java.util.Properties
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{Consumer, ConsumerConfig, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.joda.time.DateTime
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}

import scala.collection.JavaConverters._
import scala.collection.mutable

class KafkaConsumerTest extends AnyFunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach with BeforeAndAfterAll {

	private lazy val transformer = new SampleTransformer
	private lazy val api = new ZiqniApiTest

	/**
	  * Edit this value in order to do integraiton testing.
	  */
	private val enableTransformer = false

	private val numberOfMessagesToConsume = 10

	/**
	  * Kafka config
	  */
	private val isSSLEnabled = true
	private val sslBrokerEndpoints = ""
	private val plainTextBrokerEndpoints = ""
	private val topicName = ""

	// create instance for properties to access producer configs
	private lazy val props: Properties = new Properties()
	props.put(ConsumerConfig.GROUP_ID_CONFIG, "Test-Connectivity")
	props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
	props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
	props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

	private val offsets: util.List[TopicPartition] = new util.ArrayList[TopicPartition]()


	describe("Test kafka client connectivity to the broker.") {

		it("should retrieve the topic metadata on succcessfull connection.") {

			if(isSSLEnabled)
				appendSSLInformationToProperties()
			else {
				assert(plainTextBrokerEndpoints.nonEmpty, "Broker endpoints are empty.")
				props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, plainTextBrokerEndpoints)
				props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.PLAINTEXT.name)
			}

			val consumer:Consumer[String, String] = new KafkaConsumer[String, String](props)

			When("the events are consumed")
			val result = simpleConsumer(topicName, numberOfMessagesToConsume, consumer)

			Then("it returns some messages")
			assert(result.nonEmpty, s"No messages were recevied.")

			if (enableTransformer) {

				info (s"Total events recevied: ${api.eventsReceivedForTest.size}")

				assert(api.eventActionsForTest.nonEmpty)
			}
		}
	}

	private def simpleConsumer(topicName: String, stopListeningAfter: Int, consumer: Consumer[String, String] ): mutable.Map[String, String] = {
		val messages: mutable.Map[String, String] = mutable.HashMap()

		consumer.subscribe(Seq(topicName).asJava)

		println(s"Available topics ${consumer.listTopics()}")

		consumer.seekToEnd(offsets)

		var keepListening = true

		while(keepListening) {
			val records: ConsumerRecords[String, String] = consumer.poll(100)
			val ite = records.iterator()
			while (ite.hasNext) {
				val r = ite.next()
				println(s"Consumed message information: offset - [${r.offset()}] - timestamp - [${new DateTime(r.timestamp())}] - key - [${r.key()}]")
				messages.put(r.key, r.value)

				if(enableTransformer)
					transformer.kafka(r.key.getBytes, r.value.getBytes, api)

				if(messages.size == stopListeningAfter)
					keepListening = false
			}
		}

		consumer.close()

		messages
	}

	private def appendSSLInformationToProperties(): Unit = {
		assert(sslBrokerEndpoints.nonEmpty, "Broker endpoints are empty.")

		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, sslBrokerEndpoints)
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "src/test/resources/ssl-certs/<jks-name>")
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "***")

		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "src/test/resources/ssl-certs/<jks-name>")
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "***")
		// props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "***")
	}
}

