package com.ziqni.transformer.test.api

import com.ziqni.admin.sdk.model.Space
import com.ziqni.transformer.test.models._
import com.ziqni.transformer.test.store.ZiqniStores
import com.ziqni.transformers.domain._
import com.ziqni.transformers.{ZiqniApi, ZiqniApiAsync}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.jdk.FutureConverters.CompletionStageOps

import com.ziqni.admin.sdk.model.UnitOfMeasureType
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.OptionConverters.RichOptional
import scala.language.implicitConversions

final case class ZiqniApiAsyncClient(ziqniStores: ZiqniStores, masterAccount: Option[Space], accountId: AccountId, spaceName: SpaceName, _subAccounts: TrieMap[SpaceName, ZiqniApi], actions: ConcurrentHashMap[String, Seq[String]]) extends ZiqniApiAsync {
	assert(accountId != null)

	implicit private def stringToOption(s: String): Option[String] = Option(s)

	// ASYNC //
	private val transformerExecutionContext = ZiqniApiClient.GlobalZiqniApiClientContext
	override def pushEventTransaction(event: BasicEventModel): Future[Boolean] =
		ziqniStores.eventsStore.pushEventTransaction(event).asScala.map(r => r.getMeta.getErrorCount == 0)(transformerExecutionContext)

	override def findByBatchId(batchId: String): Future[Seq[BasicEventModel]] =
		ziqniStores.eventsStore.findByBatchId(batchId).asScala.map(x=>x.asScala.toSeq)(transformerExecutionContext)

	override def pushEvent(basicEventModel: BasicEventModel): Future[Boolean] =
		ziqniStores.eventsStore.pushEvent(basicEventModel).asScala.map(r => r.getMeta.getErrorCount == 0)(transformerExecutionContext)

	override def pushEvents(events: Seq[BasicEventModel]): Future[Boolean] =
		ziqniStores.eventsStore.pushEvent(events.asJava).asScala.map(r => r.getMeta.getErrorCount == 0)(transformerExecutionContext)

	override def memberIdFromMemberRefId(memberReferenceId: String): Future[Option[String]] =
		ziqniStores.membersStore.getIdByReferenceId(memberReferenceId).asScala.map(x => x.toScala)(transformerExecutionContext)

	override def memberRefIdFromMemberId(memberId: String): Future[Option[String]] =
		ziqniStores.membersStore.getRefIdByMemberId(memberId).asScala.map(x => Option(x))(transformerExecutionContext)

	override def createMember(memberReferenceId: String, displayName: String, tags: Seq[String], metaData: Option[Map[String, String]]): Future[Option[String]] =
		ziqniStores.membersStore.create(memberReferenceId, displayName, tags, metaData).asScala.map(x => x.toScala)(transformerExecutionContext)

	override def updateMember(memberId: String, memberReferenceId: Option[String], displayName: Option[String], tagsToUpdate: Option[Seq[String]], metaData: Option[Map[String, String]]): Future[Option[String]] =
		ziqniStores.membersStore.update(memberId, memberReferenceId, displayName, tagsToUpdate, metaData).asScala.map(_=>Option(memberId))(transformerExecutionContext)

	override def getMember(memberId: String): Future[Option[BasicMemberModel]] =
		ziqniStores.membersStore.findBasicMemberModelById(memberId).asScala.map(x=>x.toScala)(transformerExecutionContext)

	override def productIdFromProductRefId(productReferenceId: String): Future[Option[String]] =
		ziqniStores.productsStore.getIdByReferenceId(productReferenceId).asScala.map(x=>x.toScala)(transformerExecutionContext)

	override def productRefIdFromProductId(productId: String): Future[Option[String]] =
		ziqniStores.productsStore.getRefIdByProductId(productId).asScala.map(x=>Option(x))(transformerExecutionContext)

	override def createProduct(productReferenceId: String, displayName: String, providers: Seq[String], productType: String, defaultAdjustmentFactor: Double, metaData: Option[Map[String, String]]): Future[Option[String]] =
		ziqniStores.productsStore.create(productReferenceId,displayName,providers,productType,defaultAdjustmentFactor,metaData).asScala.map(x=>x.toScala)(transformerExecutionContext)

	override def updateProduct(productId: String, productReferenceId: Option[String], displayName: Option[String], providers: Option[Seq[String]], productType: Option[String], defaultAdjustmentFactor: Option[Double], metaData: Option[Map[String, String]]): Future[Option[String]] =
		ziqniStores.productsStore.update(productId, productReferenceId, displayName, providers, productType, defaultAdjustmentFactor.map(_.doubleValue()), metaData).asScala.map(x=>x.toScala.map(y=>y.getId))(transformerExecutionContext)

	override def deleteProduct(productId: String): Future[Boolean] =
		ziqniStores.productsStore.delete(productId).asScala.map(x=>x.isPresent)(transformerExecutionContext)

	override def getProduct(productId: String): Future[Option[BasicProductModel]] =
		ziqniStores.productsStore.findBasicProductModelById(productId).asScala.map(x=>x.toScala)(transformerExecutionContext)

	override def eventActionExists(action: String): Future[Boolean] =
		ziqniStores.actionTypesStore.actionTypeExists(action).asScala.map(x=>x.booleanValue())(transformerExecutionContext)

	override def createEventAction(action: String, name: Option[String], metaData: Option[Map[String, String]], unitOfMeasureKey: Option[String]): Future[Boolean] =
		ziqniStores.actionTypesStore.create(action, name, metaData, unitOfMeasureKey.orNull).asScala.map(x=>x.isPresent)(transformerExecutionContext)

	override def updateEventAction(action: String, name: Option[String], metaData: Option[Map[String, String]], unitOfMeasureType: Option[String]): Future[Boolean] =
		ziqniStores.actionTypesStore.update(action,name,metaData,unitOfMeasureType).asScala.map(x => !x.getResults.isEmpty)(transformerExecutionContext)

	override def getAchievement(achievementId: String): Future[Option[BasicAchievementModel]] =
		ziqniStores.achievementsStore.getAchievement(achievementId).asScala.map(x=>x.toScala)(transformerExecutionContext)

	override def getContest(contestId: String): Future[Option[BasicContestModel]] =
		ziqniStores.contestsStore.getBasicContestModel(contestId).asScala.map(x=>x.toScala)(transformerExecutionContext)

	override def getReward(rewardId: String): Future[Option[BasicRewardModel]] =
		ziqniStores.rewardStore.getBasicReward(rewardId).asScala.map(x=>x.toScala)(transformerExecutionContext)
	override def getAward(awardId: String): Future[Option[BasicAwardModel]] =
		ziqniStores.awardStore.getBasicAward(awardId).asScala.map(x => x.toScala)(transformerExecutionContext)

	override def getUnitOfMeasure(unitOfMeasureId: String): Future[Option[BasicUnitOfMeasureModel]] =
		ziqniStores.unitsOfMeasureStore.getBasicUnitOfMeasure(unitOfMeasureId).asScala.map(x=>x.toScala)(transformerExecutionContext)

	override def getUoMMultiplierFromKey(unitOfMeasureKey: String): Future[Option[Double]] =
		ziqniStores.unitsOfMeasureStore.getUnitOfMeasureMultiplier(unitOfMeasureKey).asScala.map(x => x.toScala.map(y => y.doubleValue()))(transformerExecutionContext)

	override def createUnitOfMeasure(key: String, name: String, isoCode: Option[String], multiplier: Double, unitOfMeasureType: Option[String]): Future[Option[String]] =
		ziqniStores.unitsOfMeasureStore.create(key,Option(name), isoCode, multiplier, UnitOfMeasureType.fromValue(unitOfMeasureType.getOrElse(UnitOfMeasureType.OTHER.getValue))).asScala.map(x=>x.toScala.map(y=>y.getExternalReference))(transformerExecutionContext)
}
