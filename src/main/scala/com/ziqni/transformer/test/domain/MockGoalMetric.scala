package com.ziqni.transformer.test.domain

import com.ziqni.admin.sdk.model.GoalMetrics
import com.ziqni.transformers.domain.ZiqniGoalMetric

import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

case class MockGoalMetric(goalMetric: GoalMetrics) extends ZiqniGoalMetric {

  def asZiqniGoalMetric: ZiqniGoalMetric = this

  override def getMemberId: String = goalMetric.getMemberId

  override def getEntityId: String = goalMetric.getEntityId

  override def getValue: BigDecimal =   goalMetric.getValue

  override def getPercentageComplete: Double = goalMetric.getPercentageComplete

  override def getMostSignificantScores: List[Double] = goalMetric.getMostSignificantScores.asScala.map(_.toDouble).toList

  override def getTimestamp: Long =  goalMetric.getTimestamp

  override def getUpdateCount: Long = goalMetric.getUpdateCount

  override def getEntityType: String = goalMetric.getEntityType

  override def getMarkerTimeStamp: Long = goalMetric.getMarkerTimeStamp

  override def getGoalReached: Boolean = goalMetric.getGoalReached

  override def getStatusCode: Integer = goalMetric.getStatusCode

  override def getPosition: Integer = goalMetric.getPosition

  override def getUserDefinedValues: Map[String, Double] = goalMetric.getUserDefinedValues.asScala.map { case (k, v) => (k, v.toDouble) }.toMap
}
