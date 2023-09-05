package com.ziqni.transformer.test.models

import com.ziqni.admin.sdk.model.Member
import com.ziqni.transformers.domain.{CustomFieldEntry, ZiqniMember}

import scala.collection.JavaConverters

case class ZiqniMember(member: Member) extends com.ziqni.transformers.domain.ZiqniMember with ZiqniHelper {

	override def getMemberRefId: MemberRefId = member.getMemberRefId

	override def getDisplayName: Option[String] = Option(member.getName)

	override def getTags: Option[Seq[String]] = Option(member.getTags).map(t => JavaConverters.asScalaIteratorConverter(t.iterator()).asScala.toSeq)

	override def getMetaData: Option[Map[String, String]] = member.getMetadata

	override def getMemberId: MemberId = member.getId

	override def getCustomFields: Map[String, CustomFieldEntry[_]] = Map.empty
}
