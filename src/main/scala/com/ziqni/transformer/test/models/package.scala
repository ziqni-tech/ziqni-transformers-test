package com.ziqni.transformer.test

import org.joda.time.format.DateTimeFormatter
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.text.SimpleDateFormat

package object models {

	type AccountId = String
	type SpaceName = String
	type MemberId = String
	type MemberRefId = String
	type ProductId = String
	type ProductRefId = String
	type UnitOfMeasureId = String
	type AwardId = String
	type RewardId = String
	type StatusCode = Int
	type EntityId = String
	type EntityType = String

	object FormatDateTime {
		private val pattern1: String = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
		private val pattern2: String = "yyyy-MM-dd'T'HH:mm:ssZZ"

		val DateFormatter = new SimpleDateFormat("yyyy-MM-dd")
		val TimeFormatter = new SimpleDateFormat("HH:mm:ss")
		val DateTimeFormatter = new SimpleDateFormat(pattern2)
		val DateTimeMillisFormatter = new SimpleDateFormat(pattern1)

		val JodaDateTimeMillisFormatter: DateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern(pattern1)
		val JodaDateTimeFormatter: DateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern(pattern2)

		def parseDateTime(input: String): Option[DateTime] = {
			scala.util.control.Exception.allCatch[DateTime].opt(DateTime.parse(input, JodaDateTimeMillisFormatter).withZone(DateTimeZone.UTC)) match {
				case Some(d) => Option(d)
				case None => scala.util.control.Exception.allCatch[DateTime].opt(DateTime.parse(input, JodaDateTimeFormatter))
			}
		}

		def dateFormatter(format: String) = new SimpleDateFormat(format)
		def getFriendlyDate(dateTime: DateTime, format: String): String = dateFormatter(format).format(dateTime.toDate)
	}


	object Json {
		implicit val formats = DefaultFormats
		import org.json4s.JsonAST.JString
		import org.json4s._
		import org.json4s.jackson.Serialization


		case object JodaDateTimeSerializer extends CustomSerializer[DateTime](ser = _ => ( {
			case JString(s) => FormatDateTime.parseDateTime(s).get.withZone(DateTimeZone.UTC)
			case JNull => null
		}, {
			case d: DateTime =>
				JString(d.toString(FormatDateTime.JodaDateTimeMillisFormatter))
		}))

		def toJsString(objectToWrite: AnyRef): String =
			Serialization.write(objectToWrite)

		def toJsValue(objectToWrite: AnyRef): JValue =
			Extraction.decompose(objectToWrite)

		def fromJsonAsOption[T](jsonString: String)(implicit mf: Manifest[T]): Option[T] =
			Option(Serialization.read[T](jsonString))

		def fromJsonAsSeqOption[T](jsonString: String)(implicit mf: Manifest[T]): Option[Seq[T]] =
			Option(Serialization.read[Seq[T]](jsonString))

		def fromJsonToString(obj: JValue): String =
			JsonMethods.compact(JsonMethods.render(obj))

		def fromJsonToString(obj: JObject): String =
			JsonMethods.compact(JsonMethods.render(obj))

		def fromJsonToString(obj: JArray): String =
			JsonMethods.compact(JsonMethods.render(obj))

		def parse(str: String): JValue =
			org.json4s.native.JsonMethods.parse(str)

		def getFromJValue[T <: Any: Manifest](jValue: JValue, key: String): T =
			jValue.\(key).extract[T]

		def getFromJValue[T <: Any: Manifest](a: JValue): T =
			a.extract[T]

		def getFromJValueAsOption[T <: Any: Manifest](jValue: JValue, key: String): Option[T] = {
			val lookup = jValue.\(key)
			if(lookup.canEqual(JNothing)) None else Option( lookup.extract[T] )
		}

		def keyExists(jValue: JValue, key: String): Boolean = if(jValue.\(key).canEqual(JNothing)) false else true
	}

}
