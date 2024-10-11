package com.ziqni.transformer.test.models

import com.ziqni.admin.sdk.model.Award

case class ZiqniAward(award: Award) extends com.ziqni.transformers.domain.ZiqniAward with ZiqniHelper {

	override def getEntityId: EntityId = award.getEntityId

	override def getEntityType: EntityType = award.getEntityType.toString

	override def getMemberId: MemberId = award.getMemberId

	override def getAwardId: AwardId = award.getId

	override def getRewardRank: String = award.getRewardRank

	override def getRewardName: String = award.getRewardId

	override def getRewardValue: Double = award.getRewardValue

	override def getRewardTypeKey: String = award.getRewardType.getKey

	override def getRewardDescription: String = "";

	override def getRewardTypeId: RewardId = award.getRewardType.toString

	override def getRewardId: String = award.getRewardId

	override def getRewardMetaData: Option[Map[String, String]] = award.getMetadata

	override def claimed: Boolean = {

		false
	}//award.claimed


	override def getStatusCode: StatusCode = award.getStatusCode

	override def getActiveFrom: Long = award.getActiveFrom.toEpochSecond

	override def getActiveUntil: Long = award.getActiveUntil.toEpochSecond
}
