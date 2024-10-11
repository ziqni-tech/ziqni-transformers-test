/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.test.domain

import com.ziqni.transformers.domain.ZiqniAward

class MockAward(
						entityId: String,
						entityType: String,
						memberId: String,
						isClaimed: Boolean,
						awardId: String,
						rewardRank: String,
						rewardName: String,
						rewardValue: Double,
						rewardTypeKey: String,
						rewardDescription: String,
						rewardTypeId: String,
						rewardId: String,
						rewardMetaData: Option[Map[String, String]]

					) extends com.ziqni.transformers.domain.ZiqniAward {

	override def getEntityId: String = entityId

	override def getEntityType: String = entityType

	override def getMemberId: String = memberId

	override def claimed: Boolean = isClaimed

	override def getAwardId: String = awardId

	override def getRewardRank: String = rewardRank

	override def getRewardName: String = rewardName

	override def getRewardValue: Double = rewardValue

	override def getRewardTypeKey: String = rewardTypeKey

	override def getRewardDescription: String = rewardDescription

	override def getRewardTypeId: String = rewardTypeId

	override def getRewardId: String = rewardId

	override def getRewardMetaData: Option[Map[String, String]] = rewardMetaData

	override def getStatusCode: Int = ???

	override def getActiveFrom: Long = ???

	override def getActiveUntil: Long = ???
}
