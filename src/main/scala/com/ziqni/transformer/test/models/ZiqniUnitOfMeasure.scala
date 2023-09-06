package com.ziqni.transformer.test.models

import com.ziqni.admin.sdk.model.UnitOfMeasure

case class ZiqniUnitOfMeasure(uom: UnitOfMeasure) extends com.ziqni.transformers.domain.ZiqniUnitOfMeasure with ZiqniHelper {

	override def getName: String  = uom.getName
	override def getUnitOfMeasureKey: String  = uom.getKey

	override def getIsoCode: Option[String] = Option(uom.getIsoCode)

	override def getUnitOfMeasureId: UnitOfMeasureId = uom.getId

	override def getMultiplier: Double = uom.getMultiplier

	override def getUnitOfMeasureType: String = uom.getUnitOfMeasureType.toString

	override def getMetaData: Option[Map[String, String]] = uom.getMetadata
}
