package com.ziqni.transformer.test.api

import com.typesafe.scalalogging.LazyLogging
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

	def nextId: String = ZiqniApiClient.timeBasedUUIDGenerator.getBase64UUID
}

final case class ZiqniApiClient(async:ZiqniApiAsyncClient, masterAccount: Option[Space], accountId: AccountId, spaceName: SpaceName, _subAccounts: TrieMap[SpaceName, ZiqniApi], actions: ConcurrentHashMap[String, Seq[String]]) extends ZiqniApi with LazyLogging {
	assert(accountId != null)

	implicit private def stringToOption(s: String): Option[String] = Option(s)

	override def subAccounts: Map[SpaceName, ZiqniApi] = _subAccounts.toMap

	override def getSubAccount(spaceName: SpaceName): Option[ZiqniApi] = _subAccounts.get(spaceName)

	// UTILS //

	override def nextId: String = ZiqniApiClient.nextId

	// DEPRECATED //

	private def synchronous[T](fut: () => Future[T]): T = {
		implicit val executionContext: ExecutionContextExecutor = ZiqniApiClient.GlobalZiqniApiClientContext
		val out = Await.ready(fut.apply(), Duration.apply(60,java.util.concurrent.TimeUnit.SECONDS))
		out.value.get.get
	}

	override def pushEvents(events: Seq[BasicEventModel]): Boolean = synchronous[Boolean]( () =>
		async.pushEvents(events)
	)

	override def pushEvent(basicEventModel: BasicEventModel): Boolean = synchronous( () =>
		async.pushEvent(basicEventModel)
	)

	override def memberIdFromMemberRefId(memberReferenceId: String): Option[MemberId] = synchronous( () =>
		async.memberIdFromMemberRefId(memberReferenceId)
	)

	override def createMember(memberReferenceId: String, displayName: String, tags: Seq[String], metaData: Option[Map[String, String]] = None): Option[MemberId] = synchronous( () =>
		async.createMember(memberReferenceId,displayName,tags, metaData)
	)

	override def updateMember(memberId: String, memberReferenceId: Option[String], displayName: Option[String], tagsToUpdate: Option[Seq[String]], metaData: Option[Map[String, String]]): Option[String] = synchronous(() =>
		async.updateMember(memberId, memberReferenceId, displayName, tagsToUpdate, metaData)
	)

	override def getMember(memberId: String): Option[BasicMemberModel] = synchronous(() =>
		async.getMember(memberId)
	)

	override def productIdFromProductRefId(productReferenceId: String): Option[String] = synchronous(() =>
		async.productIdFromProductRefId(productReferenceId)
	)

	override def createProduct(productReferenceId: String, displayName: String, providers: Seq[String], productType: String, defaultAdjustmentFactor: Double, metaData: Option[Map[String, String]] = None): Option[String] = synchronous(() =>
		async.createProduct(productReferenceId,displayName, providers, productType, defaultAdjustmentFactor, metaData)
	)

	override def updateProduct(productId: String, productReferenceId: Option[String], displayName: Option[String], providers: Option[Seq[String]], productType: Option[String], defaultAdjustmentFactor: Option[Double], metaData: Option[Map[String, String]]): Option[String] = synchronous(() =>
		async.updateProduct(productId,productReferenceId, displayName, providers, productType, defaultAdjustmentFactor, metaData)
	)

	override def getProduct(productId: String): Option[BasicProductModel] = synchronous(() =>
		async.getProduct(productId)
	)

	override def deleteProduct(productId: String): Boolean = synchronous(() =>
		async.deleteProduct(productId)
	)

	override def eventActionExists(action: String): Boolean = synchronous(() =>
		async.eventActionExists(action)
	)

	override def createEventAction(action: String, name: Option[String], metadata: Option[Map[String, String]], unitOfMeasureKey: Option[String]): Boolean = synchronous(() =>
		async.createEventAction(action,name,metadata, unitOfMeasureKey)
	)

	override def updateEventAction(action: String, name: Option[String], metadata: Option[Map[String, String]], unitOfMeasureType: Option[String]): Boolean = synchronous(() =>
		async.updateEventAction(action, name, metadata, unitOfMeasureType)
	)

	override def productRefIdFromProductId(productId: String): Option[ProductRefId] = synchronous(() =>
		async.productRefIdFromProductId(productId)
	)

	override def getAchievement(achievementId: String): Option[BasicAchievementModel] = synchronous(() =>
		async.getAchievement(achievementId)
	)

	override def getReward(rewardId: String): Option[BasicRewardModel] = synchronous(() =>
		async.getReward(rewardId)
	)

	override def getAward(awardId: String): Option[BasicAwardModel] = synchronous(() =>
		async.getAward(awardId)
	)

	override def getUnitOfMeasure(unitOfMeasureId: String): Option[BasicUnitOfMeasureModel] = synchronous(() =>
		async.getUnitOfMeasure(unitOfMeasureId)
	)

	override def getUoMMultiplierFromKey(unitOfMeasureKey: String): Option[Double] = synchronous(() =>
		async.getUoMMultiplierFromKey(unitOfMeasureKey)
	)

	override def getContest(contestId: String): Option[BasicContestModel] = synchronous(() =>
		async.getContest(contestId)
	)

	override def memberRefIdFromMemberId(memberId: String): Option[String] = synchronous(() =>
		async.memberRefIdFromMemberId(memberId)
	)

	override def spotExchangeRate(fromCurrency: String, toCurrency: String, pointInTime: DateTime): Double = {
		throw new NotImplementedError()
	}
}
