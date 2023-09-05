package com.ziqni.transformer.test.api

import com.ziqni.admin.sdk.model.Space
import com.ziqni.admin.sdk.util.TimeBasedUUIDGenerator
import com.ziqni.transformer.test.models.{AccountId, MemberId, ProductRefId}
import com.ziqni.transformers.ZiqniContext._
import com.ziqni.transformers._
import com.ziqni.transformers.domain._
import org.joda.time.DateTime

import java.util.concurrent.{ConcurrentHashMap, Executors}
import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.language.implicitConversions

/**
 * https://docs.scala-lang.org/overviews/core/futures.html
 */
object ZiqniApiClient {

	type AccountId = String
	type MemberId = String
	type ProductRefId = String

	val GlobalZiqniApiClientContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

	final val timeBasedUUIDGenerator = new TimeBasedUUIDGenerator()
}

final case class ZiqniApiClient(async:ZiqniApiAsyncClient, masterAccount: Option[Space], accountId: AccountId, spaceName: SpaceName, _subAccounts: TrieMap[SpaceName, ZiqniApi], actions: ConcurrentHashMap[String, Seq[String]]) extends ZiqniApi {
	assert(accountId != null)

	implicit private def stringToOption(s: String): Option[String] = Option(s)

	override def subAccounts: Map[SpaceName, ZiqniApi] = _subAccounts.toMap

	override def getSubAccount(spaceName: SpaceName): Option[ZiqniApi] = _subAccounts.get(spaceName)

	// UTILS //

	override def nextId: String = ZiqniApiClient.timeBasedUUIDGenerator.getBase64UUID

	// DEPRECATED //

	private def synchronous[T](fut: () => Future[T]): T = {
		val out = Await.ready(fut.apply(), Duration.apply(60, java.util.concurrent.TimeUnit.SECONDS))
		out.value.get.get
	}

	private def synchronousAsOption[T](fut: () => Future[T]): Option[T] = {
		val out = Await.ready(fut.apply(), Duration.apply(60, java.util.concurrent.TimeUnit.SECONDS))

		if (out.value.get.isFailure)
			None
		else
			Option(out.value.get.get)
	}

	override def pushEvents(events: Seq[ZiqniEvent]): Boolean = synchronous[Boolean](() =>
		async.pushEvents(events)
	)

	override def pushEvent(ziqniEvent: ZiqniEvent): Boolean = synchronous(() =>
		async.pushEvent(ziqniEvent)
	)

	override def memberIdFromMemberRefId(memberReferenceId: String): Option[ZiqniMember] = synchronousAsOption(() =>
		async.memberFromMemberRefId(memberReferenceId)
	)

	override def createMember(createMember: CreateMemberRequest): Option[MemberId] = synchronousAsOption(() =>
		async.createMember(createMember)
	).map(_.getMemberId)

	override def updateMember(memberId: String, memberReferenceId: Option[String], displayName: Option[String], tagsToUpdate: Option[Seq[String]], metadata: Option[Map[String, String]]): Option[String] = synchronousAsOption(() =>
		async.updateMember(memberId, memberReferenceId, displayName, tagsToUpdate, None, metadata)
	).map(_.getMemberId)

	override def getMember(memberId: String): Option[ZiqniMember] = synchronousAsOption(() =>
		async.getMember(memberId)
	)

	override def productFromProductRefId(productReferenceId: String): Option[ZiqniProduct] = synchronousAsOption(() =>
		async.productFromProductRefId(productReferenceId)
	)

	override def createProduct(createProduct: CreateProductRequest): Option[String] = synchronousAsOption(() =>
		async.createProduct(createProduct)
	).map(_.getProductId)

	override def updateProduct(productId: String, productReferenceId: Option[String], displayName: Option[String], tags: Option[Seq[String]], defaultAdjustmentFactor: Option[Double], metadata: Option[Map[String, String]]): Option[String] = synchronousAsOption(() =>
		async.updateProduct(productId, productReferenceId, displayName, tags, defaultAdjustmentFactor, None, metadata)
	).map(_.getProductId)

	override def getProduct(productId: String): Option[ZiqniProduct] = synchronousAsOption(() =>
		async.getProduct(productId)
	)

	override def deleteProduct(productId: String): Boolean = synchronous(() =>
		async.deleteProduct(productId)
	)

	override def eventActionExists(action: String): Boolean = synchronous(() =>
		async.eventActionExists(action)
	)

	override def createEventAction(action: String, name: Option[String], metadata: Option[Map[String, String]], unitOfMeasureKey: Option[String]): Boolean = synchronous(() =>
		async.createEventAction(CreateEventActionRequest(action, name, unitOfMeasureKey, metadata.getOrElse(Map.empty)))
	)

	override def updateEventAction(action: String, name: Option[String], metadata: Option[Map[String, String]], unitOfMeasureType: Option[String]): Boolean = synchronous(() =>
		async.updateEventAction(action, name, metadata, unitOfMeasureType)
	)

	override def productRefIdFromProductId(productId: String): Option[ProductRefId] = synchronousAsOption(() =>
		async.productRefIdFromProductId(productId)
	)

	override def getAchievement(achievementId: String): Option[ZiqniAchievement] = synchronousAsOption(() =>
		async.getAchievement(achievementId)
	)

	override def getReward(rewardId: String): Option[ZiqniReward] = synchronousAsOption(() =>
		async.getReward(rewardId)
	)

	override def getAward(awardId: String): Option[ZiqniAward] = synchronousAsOption(() =>
		async.getAward(awardId)
	)

	override def getUnitOfMeasure(unitOfMeasureId: String): Option[ZiqniUnitOfMeasure] = synchronous(() =>
		async.getUnitOfMeasure(unitOfMeasureId)
	)

	override def getUoMMultiplierFromKey(unitOfMeasureKey: String): Option[Double] = synchronousAsOption(() =>
		async.getUoMMultiplierFromKey(unitOfMeasureKey)
	)

	override def getContest(contestId: String): Option[ZiqniContest] = synchronousAsOption(() =>
		async.getContest(contestId)
	)

	override def memberRefIdFromMemberId(memberId: String): Option[String] = synchronousAsOption(() =>
		async.memberRefIdFromMemberId(memberId)
	)

	override def spotExchangeRate(fromCurrency: String, toCurrency: String, pointInTime: DateTime): Double = {
		throw new NotImplementedError()
	}
}
