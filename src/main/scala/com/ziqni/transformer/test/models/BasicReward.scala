package com.ziqni.transformer.test.models

import com.ziqni.admin.sdk.model.Reward
import com.ziqni.transformers.domain.BasicRewardModel

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

case class BasicReward(reward: Reward) extends BasicRewardModel with BasicModelHelper {
	implicit private def stringToOption(s: String): Option[String] = Option(s)

	override def getClRewardId: String = reward.getId

	override def getEntityId: EntityId = reward.getEntityId

	override def getRank: String = reward.getRewardRank

	override def getName: String = getClRewardId //reward.rewardName

	override def getDescription: String = reward.getDescription.getOrElse("")

	override def getDelay: StatusCode = reward.getDelay

	override def getValue: Double = reward.getRewardValue

	override def getRewardTypeId: String = reward.getRewardType.toString

	override def getRewardTypeKey: String = reward.getRewardType.toString

	override def getRewardTypeName: String = reward.getRewardType.toString

	override def getMetaData: Option[Map[String, String]] = reward.getMetadata

	override def setMetaData(metadata: Map[String, String]): Unit = {

		val result = mergeMetaData(reward.getMetadata,metadata, merged => {
			reward.metadata(merged.asJava)
		})
	}

	/**
	 *
	 * @return Entity type this Reward is linked to
	 */
	override def getEntityType: String = reward.getEntityType.getValue
}
