/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.domain

import com.ziqni.transformers.domain.BasicAwardModel

class MockAwardModel(
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
					) extends BasicAwardModel {

	override def getEntityId: String = entityId

	override def getEntityType: String = entityType

	override def getMemberId: String = memberId

	override def claimed: Boolean = isClaimed

	override def getClAwardId: String = awardId

	override def getRewardRank: String = rewardRank

	override def getRewardName: String = rewardName

	override def getRewardValue: Double = rewardValue

	override def getRewardTypeKey: String = rewardTypeKey

	override def getRewardDescription: String = rewardDescription

	override def getRewardTypeId: String = rewardTypeId

	override def getRewardId: String = rewardId

	override def getRewardMetaData: Option[Map[String, String]] = rewardMetaData

}


object MockAwardModel {

	private var mockAwardModel: MockAwardModel = null

	val AwardIdForAchievements = "awardForAchievement"
	val AchivementEntityId = "testAchievementId"
	val AchivementEntityType = "achievement"
	val MemberId = "testMemberId"

	private def mockReard: MockRewardModel = MockRewardModel.getMockReward

	def getMockAward: MockAwardModel =
		if(mockAwardModel == null) {
			mockAwardModel = new MockAwardModel(
				AchivementEntityId,
				AchivementEntityType,
				MemberId,
				false,
				AwardIdForAchievements,
				mockReard.getRank,
				mockReard.getName,
				mockReard.getValue,
				mockReard.getRewardTypeKey,
				mockReard.getDescription,
				mockReard.getRewardTypeId,
				mockReard.getClRewardId,
				mockReard.getMetaData
			)
			mockAwardModel
		} else
			mockAwardModel
}
