package com.ziqni.transformer.test.api

import com.ziqni.admin.sdk.model.Space
import com.ziqni.transformer.test.models._
import com.ziqni.transformer.test.store.ZiqniStores
import com.ziqni.transformers.domain.{CreateEventActionRequest, CreateMemberRequest, CreateProductRequest, CustomFieldEntry, ZiqniEvent, ZiqniMember, ZiqniProduct, ZiqniUnitOfMeasure}
import com.ziqni.transformers.ZiqniApi

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.jdk.FutureConverters.CompletionStageOps
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.FunctionConverters.{enrichAsJavaFunction, enrichAsJavaSupplier}
import scala.language.implicitConversions

final case class ZiqniApiAsyncClient(ziqniStores: ZiqniStores, masterAccount: Option[Space], accountId: AccountId, spaceName: SpaceName, _subAccounts: TrieMap[SpaceName, ZiqniApi], actions: ConcurrentHashMap[String, Seq[String]])
	extends com.ziqni.transformers.ZiqniApiAsync {
	assert(accountId != null)

	override def nextId: String = ZiqniApiClient.timeBasedUUIDGenerator.getBase64UUID
	implicit private def stringToOption(s: String): Option[String] = Option(s)

	// ASYNC //
	implicit private val transformerExecutionContext: ExecutionContextExecutor = ZiqniApiClient.GlobalZiqniApiClientContext
	override def pushEventTransaction(event: ZiqniEvent): Future[Boolean] =
		ziqniStores.eventsStore.pushEventTransaction(event).asScala.map(r => r.getMeta.getErrorCount == 0)(transformerExecutionContext)

	override def findByBatchId(batchId: String): Future[Seq[ZiqniEvent]] =
		ziqniStores.eventsStore.findByBatchId(batchId).asScala.map(x=>x.asScala.toSeq)(transformerExecutionContext)

	override def pushEvent(basicEvent: ZiqniEvent): Future[Boolean] =
		ziqniStores.eventsStore.pushEvent(basicEvent).asScala.map(r => r.getMeta.getErrorCount == 0)(transformerExecutionContext)

	override def pushEvents(events: Seq[ZiqniEvent]): Future[Boolean] =
		ziqniStores.eventsStore.pushEvent(events.asJava).asScala.map(r => r.getMeta.getErrorCount == 0)(transformerExecutionContext)

	override def memberFromMemberRefId(memberReferenceId: String): Future[ZiqniMember] =
		ziqniStores.membersStore.getMemberByReferenceId(memberReferenceId).asScala

	override def memberRefIdFromMemberId(memberId: String): Future[String] =
		ziqniStores.membersStore.getRefIdByMemberId(memberId).asScala

	override def createMember(toCreate: CreateMemberRequest): Future[ZiqniMember] =
		ziqniStores.membersStore.create(toCreate).asScala

	override def updateMember(memberId: String, memberReferenceId: Option[String], displayName: Option[String], tagsToUpdate: Option[Seq[String]], customFields: Option[Map[String,CustomFieldEntry[_<:Any]]] = None, metadata: Option[Map[String, String]] = None): Future[ZiqniMember] =
		ziqniStores.membersStore.update(memberId, memberReferenceId, displayName, tagsToUpdate).asScala

	override def getMember(memberId: String): Future[ZiqniMember] =
		ziqniStores.membersStore.findZiqniMemberById(memberId).asScala

	override def productFromProductRefId(productReferenceId: String): Future[ZiqniProduct] =
		ziqniStores.productsStore.getByReferenceId(productReferenceId).asScala

	override def productRefIdFromProductId(productId: String): Future[String] =
		ziqniStores.productsStore.getRefIdByProductId(productId).asScala

	override def getOrCreateProduct(referenceId: String, createAs: () => com.ziqni.transformers.domain.CreateProductRequest): Future[ZiqniProduct] =
		ziqniStores.productsStore.getOrCreateProduct(referenceId, createAs.asJava, null).asScala

	override def updateProduct(productId: String, productReferenceId: Option[String], displayName: Option[String], tags: Option[Seq[String]], defaultAdjustmentFactor: Option[Double], customFields: Option[Map[String,CustomFieldEntry[_<:Any]]] = None, metadata: Option[Map[String, String]] = None): Future[ZiqniProduct] =
		ziqniStores.productsStore.update(productId, productReferenceId, displayName, tags, "productType", defaultAdjustmentFactor.map(_.doubleValue()), metadata).asScala //FIXME

	override def deleteProduct(productId: String): Future[Boolean] =
		ziqniStores.productsStore.delete(productId).asScala.map(x=>x.isPresent)(transformerExecutionContext)

	override def getProduct(productId: String): Future[ZiqniProduct] =
		ziqniStores.productsStore.findZiqniProductById(productId).asScala

	override def eventActionExists(action: String): Future[Boolean] =
		ziqniStores.actionTypesStore.actionTypeExists(action).asScala.map(x=>x.booleanValue())(transformerExecutionContext)

	override def getOrCreateEventAction(action: String, createAs: () => com.ziqni.transformers.domain.CreateEventActionRequest): Future[String] =
		ziqniStores.actionTypesStore.getOrCreateEventAction(action, createAs.asJava).asScala

	override def updateEventAction(action: String, name: Option[String], metaData: Option[Map[String, String]], unitOfMeasureType: Option[String]): Future[Boolean] =
		ziqniStores.actionTypesStore.update(action,name,metaData,unitOfMeasureType).asScala.map(x => !x.getResults.isEmpty)(transformerExecutionContext)

	override def getAchievement(achievementId: String): Future[ZiqniAchievement] = {
		val out:Future[ZiqniAchievement] = ziqniStores.achievementsStore.getAchievement(achievementId).asScala
		out
	}

	override def getContest(contestId: String): Future[ZiqniContest] =
		ziqniStores.contestsStore.getZiqniContest(contestId).asScala

	override def getReward(rewardId: String): Future[ZiqniReward] =
		ziqniStores.rewardStore.getZiqniReward(rewardId).asScala
	override def getAward(awardId: String): Future[ZiqniAward] =
		ziqniStores.awardStore.getZiqniAward(awardId).asScala

	override def getOrCreateUnitOfMeasure(key: String, createAs: () => com.ziqni.transformers.domain.CreateUnitOfMeasureRequest): Future[ZiqniUnitOfMeasure] =
		ziqniStores.unitsOfMeasureStore.getZiqniUnitOfMeasure(key, createAs.asJava).asScala

	override def getUoMMultiplierFromKey(unitOfMeasureKey: String): Future[Double] =
		ziqniStores.unitsOfMeasureStore.getUnitOfMeasureMultiplier(unitOfMeasureKey).asScala.map(x => x)

	override def createUnitOfMeasure(toCreate: com.ziqni.transformers.domain.CreateUnitOfMeasureRequest): Future[String] =
		ziqniStores.unitsOfMeasureStore.create(toCreate).asScala.map(x=>x.getExternalReference)(transformerExecutionContext)


	override def getOrCreateMember(referenceId: String, createAs: () => CreateMemberRequest): Future[ZiqniMember] =
		ziqniStores.membersStore.getOrCreateMember(referenceId, createAs.asJava).asScala

	override def getAndOnExitsOrCreateMember(referenceId: String, onExist: ZiqniMember => Future[ZiqniMember], createAs: () => CreateMemberRequest): Future[ZiqniMember] =
		ziqniStores.membersStore.getAndOnExitsOrCreateMember(referenceId, createAs.asJava, onExist.asJava).asScala

	override def createProduct(toCreate: CreateProductRequest): Future[ZiqniProduct] =
    ziqniStores.productsStore.create(toCreate).asScala

	override def getAndOnExitsOrCreateProduct(referenceId: String, onExist: ZiqniProduct => Future[ZiqniProduct], createAs: () => CreateProductRequest): Future[ZiqniProduct] =
		ziqniStores.productsStore.getAndOnExitsOrCreateProduct(referenceId, createAs.asJava, onExist.asJava).asScala

	override def createEventAction(toCreate: CreateEventActionRequest): Future[Boolean] =
		ziqniStores.actionTypesStore.create(toCreate).asScala.map(x => x)

	override def getUnitOfMeasure(unitOfMeasureId: String): Future[Option[ZiqniUnitOfMeasure]] =
		ziqniStores.unitsOfMeasureStore.getZiqniUnitOfMeasure(unitOfMeasureId).asScala.map( x => { if(x.isPresent) Option(x.get()) else None })
}
