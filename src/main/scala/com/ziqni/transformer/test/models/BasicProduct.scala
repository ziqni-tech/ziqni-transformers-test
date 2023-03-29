package com.ziqni.transformer.test.models

import com.ziqni.transformers.domain.BasicProductModel

import scala.jdk.CollectionConverters.ListHasAsScala

case class BasicProduct(product: com.ziqni.admin.sdk.model.Product) extends BasicProductModel with BasicModelHelper {

	override def getProductReferenceId: ProductRefId = product.getProductRefId

	override def getName: String = Option(product.getName).getOrElse(getProductReferenceId)

	override def getProviders: Array[String] =  Option(product.getTags).map(_.asScala.toArray).getOrElse(Array.empty)

	override def getProductType: String = "" //product.productType

	override def getDefaultAdjustmentFactor: Option[Double] = Option(product.getAdjustmentFactor).map(_.toDouble)

	override def getClProductId: ProductId = product.getId

	override def getMetaData: Option[Map[String, String]] = product.getMetadata
}
