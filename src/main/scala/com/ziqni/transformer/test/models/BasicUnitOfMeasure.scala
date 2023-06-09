package com.ziqni.transformer.test.models

import com.ziqni.admin.sdk.model.UnitOfMeasure
import com.ziqni.transformers.domain.BasicUnitOfMeasureModel

case class BasicUnitOfMeasure(uom: UnitOfMeasure) extends BasicUnitOfMeasureModel with BasicModelHelper {

	override def getName: String  = uom.getName
	override def getUnitOfMeasureKey: String  = uom.getKey

	override def getIsoCode: Option[String] = Option(uom.getIsoCode)

	override def getUnitOfMeasureId: UnitOfMeasureId = uom.getId

	override def getMultiplier: Double = uom.getMultiplier

	override def getUnitOfMeasureType: String = uom.getUnitOfMeasureType.toString

	override def getMetaData: Option[Map[String, String]] = uom.getMetadata
}
