package com.ziqni.transformer.test.models

import com.ziqni.admin.sdk.model.Member
import com.ziqni.transformers.domain.BasicMemberModel

import scala.collection.JavaConverters

case class BasicMember(member: Member) extends BasicMemberModel with BasicModelHelper {

	override def getMemberRefId: MemberRefId = member.getMemberRefId

	override def getDisplayName: Option[String] = Option(member.getName)

	override def getTags: Option[Seq[String]] = Option(member.getTags).map(t => JavaConverters.asScalaIteratorConverter(t.iterator()).asScala.toSeq)

	override def getMetaData: Option[Map[String, String]] = member.getMetadata

	override def getClMemberId: MemberId = member.getId
}
