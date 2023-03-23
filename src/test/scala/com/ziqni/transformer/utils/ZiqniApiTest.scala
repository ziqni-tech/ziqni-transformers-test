/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.utils

import java.util.Base64
import com.ziqni.transformers.domain._
import com.ziqni.transformers.ZiqniApi
import com.ziqni.transformer.domain.{BasicContestModelMock, MockAwardModel, MockRewardModel}

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.json4s.{DefaultFormats, JsonAST}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization

import scala.collection.concurrent.TrieMap
import scala.util.{Random, Try}

class ZiqniApiTest extends ZiqniApi {

	implicit val formats: DefaultFormats.type = DefaultFormats

	type HttpBody = String
	type UrlString = String
	type HeadersMap = Map[String, Seq[String]]

	val memberForTest = new mutable.HashMap[String, String]()

	val memberMetadataForTest = new mutable.HashMap[String, Option[Map[String, String]]]()

	val memberTagForTest = new mutable.HashMap[String, Seq[String]]()

	val productForTest = new mutable.HashMap[String, String]()

	val eventActionsForTest = new ListBuffer[String]

	val eventsReceivedForTest = new TrieMap[String, BasicEventModel]()

	val httpRequests = new mutable.HashMap[UrlString, (HttpBody, HeadersMap)]()

	val SharedSecretForTest = "SharedSecret"

	val newContest = new BasicContestModelMock
	val contestData = new mutable.HashMap[String, BasicContestModelMock]()

	val eventDataByBatch = new mutable.HashMap[String, BasicEventModel]()

	val SampleResponse = s"""{ "authCode": ${Random.nextInt(10000)} }"""

	private lazy val rewards: Map[String, BasicRewardModel] = Map(MockRewardModel.RewardIdForAchievements -> MockRewardModel.getMockReward)
	private lazy val awards: Map[String, BasicAwardModel] = Map(MockAwardModel.AwardIdForAchievements -> MockAwardModel.getMockAward)

	override def getSubAccount(spaceName: String): Option[ZiqniApi] = None

	/**
	  * Your account identifier
	  */
	override val accountId: String = "yourAccountId"

	/**
	  * Insert an event into your CompetitionLabs space
	  *
	  * @param event The event to add
	  * @return True on success, false on duplicate and exception if malformed
	  */
	override def pushEvent(event: BasicEventModel): Boolean = {
		val keyForMap = s"${event.memberRefId}:${event.entityRefId}:${event.eventRefId}:${event.action}:${event.sourceValue}"

		eventsReceivedForTest.put(keyForMap, event).nonEmpty
	}

	/**
	  * Insert a sequence of events into your CompetitionLabs space
	  *
	  * @param events The events to add
	  * @return True on success, false on duplicate and exception if malformed
	  */
	override def pushEvents(events: Seq[BasicEventModel]): Boolean = !events.forall(pushEvent)

	/**
	  * Get the CompetitionLabs id for the member based on your reference id
	  *
	  * @param memberReferenceId The id used to identify this member in the sending system
	  * @return The id used in the CompetitionLabs system or None if the user does not exist
	  */
	override def memberIdFromMemberRefId(memberReferenceId: String): Option[String] = memberForTest.get(memberReferenceId)

	/**
	  * Create a member in the CompetitionLabs system
	  *
	  * @param memberReferenceId The id used to identify this member in the sending system
	  * @param displayName       Display name
	  * @param groups            The groups to add this member to
	  * @return The id used in the CompetitionLabs system
	  */
	override def createMember(memberReferenceId: String, displayName: String, groups: Seq[String], metaData: Option[Map[String, String]]): Option[String] = {
		val key = "Z-" + memberReferenceId
		memberForTest.put(
			memberReferenceId, key
		)

		memberMetadataForTest.put(
			key, metaData
		)

		memberTagForTest.put(
			key, groups
		)

		Option(key)
	}

	/**
	  * Get the CompetitionLabs id for the product based on your reference id
	  *
	  * @param productReferenceId The id used to identify this product in the sending system
	  * @return The id used in the CompetitionLabs system or None if the product does not exist
	  */
	override def productIdFromProductRefId(productReferenceId: String): Option[String] = productForTest.get(productReferenceId)

	/**
	  *
	  * @param productReferenceId      The id used to identify this product in the sending system
	  * @param displayName             Display name
	  * @param providers               The providers of this product
	  * @param productType             The type of product
	  * @param defaultAdjustmentFactor The default adjustment factor to apply
	  * @return The id used in the CompetitionLabs system
	  */
	override def createProduct(productReferenceId: String, displayName: String, providers: Seq[String], productType: String, defaultAdjustmentFactor: Double, metaData: Option[Map[String, String]]): Option[String] = {
		val key = "Z-" + productReferenceId
		productForTest.put(productReferenceId, key)
		Option(key)
	}

	/**
	  * Verify if the event action type exists in your space
	  *
	  * @param action The action
	  * @return True of the action was created
	  */
	override def eventActionExists(action: String): Boolean = eventActionsForTest.contains(action)

	/** *
	  * Create the action in your space
	  *
	  * @param action True on success false on failure
	  * @return
	  */
	override def createEventAction(action: String, name: Option[String] = None, metaData: Option[Map[String, String]] = None): Boolean = {
		eventActionsForTest += action
		true
	}

	/** *
	  * Generate a unique time based UUID, this can be used to set the batchId value if
	  * a single event is transformed into multiple distinct events (facts) and a correlation
	  * needs to be maintained
	  *
	  * @return A time based UUID as a string
	  */
	override def nextId = "ABCDEFGHIK"

	/**
	  * Get the space name associated with this account
	  */
	override val spaceName: String = "yourspace"

	/**
	  * Get the member reference id for the member based on CompetitionLabs id
	  *
	  * @param memberId The id used to identify this member in the sending system
	  * @return The id used in the CompetitionLabs system or None if the user does not exist
	  */
	override def memberRefIdFromMemberId(memberId: String): Option[String] =  memberForTest.find(_._2 == memberId).map(_._1)

	/**
	  * Get the product id for the product based on your CompetitionLabs id
	  *
	  * @param productId The id used to identify this product in the sending system
	  * @return The id used in the CompetitionLabs system or None if the product does not exist
	  */
	override def productRefIdFromProductId(productId: String): Option[String] = productForTest.find(_._2 == productId).map(_._1)

	/**
	  * Converts a json string to a JValue
	  *
	  * @param body The string to deserialise
	  * @return JValue or throws exception
	  */
	override def fromJsonString(body: String): JsonAST.JValue = parse(body)

	/**
	  * Converts a map to a json string
	  *
	  * @param obj The object to serialise
	  * @return json string or throws exception
	  */
	override def toJsonFromMap(obj: Map[String, Any]): String = Serialization.write(obj)

	/**
	  * Send a http get request
	  *
	  * @param url                  The url to send the request too
	  * @param headers              The request headers
	  * @param basicAuthCredentials Basic authentication
	  * @return The http status code or -1 on error
	  */
	override def httpGet(url: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {
		httpRequests.put(url, ("", headers))
		HttpResponseEntity(statusCode = 200, content = "This is test")
	}

	/**
	  * Send a http put request
	  *
	  * @param url                  The url to send the request too
	  * @param body                 The request body
	  * @param headers              The request headers
	  * @param basicAuthCredentials Basic authentication
	  * @return The http status code or -1 on error
	  */
	override def httpPut(url: String, body: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {
		httpRequests.put(url, (body, headers))
		HttpResponseEntity(statusCode = 200, content = "This is test")
	}

	/**
	  * Send a http post request
	  *
	  * @param url                  The url to send the request too
	  * @param body                 The request body
	  * @param headers              The request headers
	  * @param basicAuthCredentials Basic authentication
	  * @return The http status code or -1 on error
	  */
	override def httpPost(url: String, body: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {
		httpRequests.put(url, (body, headers))
		val actualEncodedRequest = headers.get("X-Signature").flatMap(_.headOption)
		val ts = headers.get("X-Timestamp").flatMap(_.headOption).getOrElse("")
		if(actualEncodedRequest.contains(hmacSha256Str(s"$body|$ts", SharedSecretForTest).get)) HttpResponseEntity(statusCode = 200, content = SampleResponse)
		else HttpResponseEntity(statusCode = 400, content = "This is test")

	}

	/**
	  * Send a http delete request
	  *
	  * @param url                  The url to send the request too
	  * @param headers              The request headers
	  * @param basicAuthCredentials Basic authentication
	  * @return The http status code or -1 on error
	  */
	override def httpDelete(url: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {
		httpRequests.put(url, ("", headers))
		HttpResponseEntity(statusCode = 200, content = "This is test")
	}

	override def getAchievement(achievementId: String): Option[BasicAchievementModel] = None

	override def getReward(rewardId: String): Option[BasicRewardModel] = rewards.get(rewardId)

	override def getAward(awardId: String): Option[BasicAwardModel] = awards.get(awardId)

	override def subAccounts: Map[String, ZiqniApi] = Map.empty

	override def updateMember(memberId: String, memberReferenceId: Option[String], displayName: Option[String], tags: Option[Seq[String]], metaData: Option[Map[String, String]]): Option[String] = {
		memberTagForTest.remove(memberId)
		memberMetadataForTest.remove(memberId)

		memberMetadataForTest.put(
			memberId, metaData
		)

		if(tags.nonEmpty)
			memberTagForTest.put(
				memberId, tags.get
			)

		None
	}

	case class BasicMember(memberId: String) extends BasicMemberModel {

		val memberRef: Option[String] = memberForTest.find(_._2 == memberId).map(_._1)

		override def getMemberRefId: String = memberRef.get

		override def getDisplayName: Option[String] = Option("tester")

		override def getTags: Option[Seq[String]] = None

		override def getMetaData: Option[Map[String, String]] = try{ memberMetadataForTest(memberId) }catch{ case e: Exception => None }

		override def getClMemberId: String = memberId
	}



	override def getMember(memberId: String): Option[BasicMemberModel] = Option(BasicMember(memberId))

	override def updateProduct(productId: String, productReferenceId: Option[String], displayName: Option[String], providers: Option[Seq[String]], productType: Option[String], defaultAdjustmentFactor: Option[Double], metaData: Option[Map[String, String]]) : Option[String] = None

	override def getProduct(productId: String): Option[BasicProductModel] = None

	override def deleteProduct(productId: String): Boolean = false

	override def getContest(contestId: String): Option[BasicContestModel] = {
		contestData.find(x => x._1 == contestId).map(x => x._2)
	}

	override def getUnitOfMeasure(unitOfMeasureId: String): Option[BasicUnitOfMeasureModel] = None

	override def getUoMMultiplierFromKey(unitOfMeasureKey: String): Option[Double] = None

	override def updateEventAction(action: String, name: Option[String], metaData: Option[Map[String, String]], unitOfMeasureType: Option[String]): Boolean = true

	/** Security */

	val encoder: Base64.Encoder = Base64.getUrlEncoder.withoutPadding()
	val decoder: Base64.Decoder = Base64.getDecoder

	private def keyToBytes(key: String) = decoder.decode(key)

	private def getHmacSha256KeySpec(key: String): SecretKeySpec = new SecretKeySpec(keyToBytes(key), "HmacSHA256")

	private def hmacSha256(in: Array[Byte], keySpec: SecretKeySpec): Option[Array[Byte]] = {
		Try {
			val mac = Mac.getInstance("HmacSHA256")
			mac.init(keySpec)
			mac.doFinal(in)
		}.toOption
	}

	def hmacSha256Str(content: String, key: String): Option[String] = {
		hmacSha256(content.getBytes("UTF-8"), getHmacSha256KeySpec(key)).map(encoder.encodeToString)
	}
}
