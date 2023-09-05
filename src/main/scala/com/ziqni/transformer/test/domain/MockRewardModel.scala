/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */
package com.ziqni.transformer.test.domain

import com.ziqni.transformers.domain.{CustomFieldEntry, ZiqniReward}

class MockReward(
						 entityId: String,
						 entityType: String,
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
					) extends com.ziqni.transformers.domain.ZiqniReward {

	override def getEntityId: String = entityId

	override def getRank: String = rank

	override def getName: String = name

	override def getDescription: String = description

	override def getDelay: Int = delay

	override def getValue: Double = rewardValue

	override def getRewardTypeId: String = rewardTypeId

	override def getRewardTypeName: String = rewardTypeName

	override def getRewardTypeKey: String = rewardTypeKey

	override def getEntityType: String = entityType

	override def getMetaData: Option[Map[String, String]] = rewardMetaData

	override def setMetaData(metadata: Map[String, String]): Unit = this.rewardMetaData = Option(metadata)

	override def getRewardId: String = rewardId

	override def getCustomFields: Map[String, CustomFieldEntry[_]] = ???
}