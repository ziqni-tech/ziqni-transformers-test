package com.ziqni.transformer
/*
 * Copyright (c) 2023. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */
import com.typesafe.scalalogging.LazyLogging
import com.ziqni.transformers.{ZiqniContext, ZiqniMqTransformer}
import org.joda.time.DateTime
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContextExecutor, Future}

class SampleTransformer extends ZiqniMqTransformer with LazyLogging {

	private implicit val formats: DefaultFormats.type = DefaultFormats

	/** Convert milliseconds time into human readable time **/
	private def timeStampToDateTime(timestamp: Long): DateTime = new DateTime(timestamp)

	/**
	 * Handle incoming message from RabbitMQ
	 * @param message
	 * @param args
	 * @param ziqniApi
	 */
	override def apply(message: Array[Byte], ziqniContext: ZiqniContext, args: Map[String, Any]): Unit = {

		val messageAsString = ZiqniContext.convertByteArrayToString(message)
		val jsonObj = ZiqniContext.fromJsonString(messageAsString)

//		jsonObj match {
//			case jArr: JArray => jArr.arr.foreach( jsonValue => handleIndividualJObject(jsonValue, ziqniContext) )
//			case _ => handleIndividualJObject(jsonObj, ziqniContext)
//		}

	}

	/**
	 * handles member creation
	 * @param memberRef external member reference
	 * @param displayName external member display name
	 * @param groups member group segmentation
	 * @param memberMeta additional metadata
	 * @param onMemberGroupUpdate on member group update callback function
	 * @param onMemberMetadataUpdate on member metadata update callback function
	 * @param ziqniApi
	 */
	private def getOrCreateMember(memberRef: String, displayName: Option[String], groups: Seq[String],
																memberMeta: Option[Map[String, String]], onMemberGroupUpdate: Seq[String] => Seq[String],
																onMemberMetadataUpdate: Option[Map[String, String]] => Option[Map[String, String]], ziqniContext: com.ziqni.transformers.ZiqniContext): Future[Option[String]] = {

		implicit val context: ExecutionContextExecutor = ziqniContext.ziqniExecutionContext

		for {
			memberIdFound <- ziqniContext.ziqniApiAsync.memberIdFromMemberRefId(memberRef)
			out <- memberIdFound match {
				case Some(mid) =>
					val member = ziqniContext.ziqniApi.getMember(mid).get

					// group update
					val mGroups = member.getTags
					val gToSet = (if (mGroups.isEmpty) groups else {
						onMemberGroupUpdate(mGroups.get)
					}).toArray

					// metadata update
					val mMetadata = member.getMetaData
					val updateMetadata = if (mMetadata.isEmpty) memberMeta else {
						onMemberMetadataUpdate(mMetadata)
					}

					// update member object
					ziqniContext.ziqniApiAsync.updateMember(mid, Option(memberRef), Option(displayName.getOrElse(memberRef)), Option(gToSet.distinct), updateMetadata)
				case _ =>
					ziqniContext.ziqniApiAsync.createMember(memberRef, displayName.getOrElse(memberRef), groups.distinct, memberMeta)
			}} yield out
	}


	// handle action creation inside in action helpers
	private def getOrCreateEventAction(action: String, ziqniContext: ZiqniContext, basicUnitOfMeasureModelKey: Option[String]): Unit = {
		implicit val context: ExecutionContextExecutor = ziqniContext.ziqniExecutionContext

		ziqniContext.ziqniApiAsync.eventActionExists(action.toLowerCase).map( exists => {
			if(!exists)
				ziqniContext.ziqniApiAsync.createEventAction(action.toLowerCase,None,None,basicUnitOfMeasureModelKey)
		})
	}

	/**
	 * handle product creation
	 * @param productReferenceId external product reference ID
	 * @param displayName external display name
	 * @param providers provider name
	 * @param productType product type
	 * @param metaData additional metadata
	 * @param ziqniApi
	 */
	private def getOrCreateProduct(productReferenceId: String, displayName: String, providers: Seq[String], productType: String, metaData: Option[Map[String, String]], ziqniContext: com.ziqni.transformers.ZiqniContext): Unit = {
		implicit val context: ExecutionContextExecutor = ziqniContext.ziqniExecutionContext

		ziqniContext.ziqniApiAsync.productIdFromProductRefId(productReferenceId).map( result => {
			if(result.isEmpty)
				ziqniContext.ziqniApiAsync.createProduct(productReferenceId, displayName, providers, productType, 1, metaData)
		})
	}



	/**
	 * Extract bet action using the game phase ID
	 *
	 * @param gamePhaseId unique round ID
	 * @param ziqniApi
	 * @return
	 */
	private def calculateWinMultiplier(totalBetAmount: Double, totalWinAmount: Double): Double = {
		totalWinAmount / totalBetAmount
	}
}
