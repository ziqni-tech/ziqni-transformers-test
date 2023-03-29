package com.ziqni.transformer.test.models

import org.joda.time.DateTime

import java.time.OffsetDateTime
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

trait BasicModelHelper {

	implicit def getMetaDataAsMap(in: java.util.Map[String, String]): Option[Map[String, String]] =
		Option(in).map(_.asScala.toMap)

	implicit def dateConversion(offsetDateTime: OffsetDateTime): DateTime = Option(offsetDateTime).map(t =>new DateTime(t.toInstant.toEpochMilli)).getOrElse(DateTime.now())

	implicit def dateConversionOption(offsetDateTime: OffsetDateTime): Option[DateTime] = Option(offsetDateTime).map(t =>new DateTime(t.toInstant.toEpochMilli))

	protected def mergeMetaData[T](sourceMetadata: java.util.Map[String, String], metadata: Map[String, String], onMerged: Map[String, String] => T ): T = {

		val sourceMeta = Option(sourceMetadata).map(_.asScala.toMap).getOrElse(Map.empty)

		val combined = sourceMeta ++ metadata

		onMerged(combined)
	}
}
