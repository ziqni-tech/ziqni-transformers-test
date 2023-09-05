/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */
package com.ziqni.transformer.test.domain

import com.ziqni.transformers.domain.{CustomFieldEntry, ZiqniContest}
import org.joda.time.DateTime

class MockContest extends com.ziqni.transformers.domain.ZiqniContest{
	val name = "test"

	override def getName: String = name

	override def getCompetitionId: String = ""

	override def getStartDate: DateTime = DateTime.now()

	override def getEndDate: Option[DateTime] = None

	override def getCreatedTime: DateTime = DateTime.now()

	override def getContestId: String = ""

	override def getProductRefIds: Option[Array[String]] = None

	override def getGroups: Option[Array[String]] = None

	override def getStatus: String = ""

	override def getMetaData: Option[Map[String, String]] = None

	override def setMetaData(metadata: Map[String, String]): Unit = None

	override def setStatus(): Unit = None

	override def getCustomFields: Map[String, CustomFieldEntry[_]] = ???
}
