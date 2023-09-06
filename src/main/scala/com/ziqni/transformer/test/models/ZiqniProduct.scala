package com.ziqni.transformer.test.models

import com.ziqni.transformers.domain

import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsScala}

case class ZiqniProduct(product: com.ziqni.admin.sdk.model.Product) extends com.ziqni.transformers.domain.ZiqniProduct with ZiqniHelper {

	override def getProductReferenceId: ProductRefId = product.getProductRefId

	override def getName: String = Option(product.getName).getOrElse(getProductReferenceId)

	override def getProviders: Array[String] =  Option(product.getTags).map(_.asScala.toArray).getOrElse(Array.empty)

	override def getProductType: String = "" //product.productType

	override def getDefaultAdjustmentFactor: Option[Double] = Option(product.getAdjustmentFactor).map(_.toDouble)

	override def getProductId: ProductId = product.getId

	override def getMetaData: Option[Map[String, String]] = product.getMetadata

	override def getCustomFields: Map[String, domain.CustomFieldEntry[_]] = convertCustomFields(product.getCustomFields.asScala.toMap)

	def asBase: com.ziqni.transformers.domain.ZiqniProduct = this
}
