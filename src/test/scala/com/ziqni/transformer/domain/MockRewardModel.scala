/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */
package com.ziqni.transformer.domain

import com.ziqni.transformers.domain.BasicRewardModel

class MockRewardModel(
						 entityId: String,
						 rank: String,
						 name: String,
						 description: String,
						 delay: Int,
						 rewardValue: Double,
						 rewardTypeId: String,
						 rewardTypeName: String,
						 rewardTypeKey: String,
						 var rewardMetaData: Option[Map[String, String]],
						 rewardId: String
					) extends BasicRewardModel {

	override def getEntityId: String = entityId

	override def getRank: String = rank

	override def getName: String = name

	override def getDescription: String = description

	override def getDelay: Int = delay

	override def getValue: Double = rewardValue

	override def getRewardTypeId: String = rewardTypeId

	override def getRewardTypeName: String = rewardTypeName

	override def getRewardTypeKey: String = rewardTypeKey

	override def getMetaData: Option[Map[String, String]] = rewardMetaData

	override def setMetaData(metadata: Map[String, String]): Unit = this.rewardMetaData = Option(metadata)

	override def getClRewardId: String = rewardId

}

object MockRewardModel {

	private var mockRewardModel: MockRewardModel = null

	val RewardIdForAchievements = "rewardForAchievement"
	val AchivementEntityId = "testAchievementId"

	def getMockReward: MockRewardModel =
		if(mockRewardModel == null) {
			mockRewardModel = new MockRewardModel(
				AchivementEntityId,
				"1",
				"Achievement Reward",
				"This is a test reward for achievement",
				0,
				1.0,
				"testRewardTypeId",
				"Free spins",
				"free-spins",
				None,
				RewardIdForAchievements)
			mockRewardModel
		} else
			mockRewardModel
}