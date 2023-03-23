/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.utils

import com.ziqni.transformers.ZiqniApiClient
import com.ziqni.transformers.domain.BasicEventModel

import scala.collection.concurrent.TrieMap

/**
	* This impl is to test the buffer for event correlation - only to be used for Sisal until notice
	*/
class ZiqniApiWithClientTest extends ZiqniApiTest with ZiqniApiClient {

	/**
		* We simulate the findByBatchId behaviour
		*/
	val eventsCorrelationMapForTest = new TrieMap[String, Seq[String]]()

	/**
		* Insert an event into your CompetitionLabs space
		*
		* @param event The event to add
		* @return True on success, false on duplicate and exception if malformed
		*/
	override def pushEventTransaction(event: BasicEventModel): Boolean = {
		val keyForMap = s"${event.memberRefId}:${event.entityRefId}:${event.eventRefId}:${event.action}:${event.sourceValue}"

		event.batchId.map(batchId => {
			if(eventsCorrelationMapForTest.contains(batchId)){
				val oldKeys = eventsCorrelationMapForTest(batchId)
				val newKeySeq = oldKeys :+ keyForMap
				eventsCorrelationMapForTest.put(batchId, newKeySeq)
			}
			else {
				eventsCorrelationMapForTest.put(batchId, Seq(keyForMap))
			}
		})

		eventsReceivedForTest.put(keyForMap, event).nonEmpty
	}

	override def findByBatchId(batchId: String): Seq[BasicEventModel] =
		eventsCorrelationMapForTest.get(batchId).map(seq => seq.flatMap(eId => eventsReceivedForTest.get(eId))).getOrElse(Seq.empty)
}
