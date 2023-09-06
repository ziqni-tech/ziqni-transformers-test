package com.ziqni.transformer.test.models

import com.typesafe.scalalogging.LazyLogging
import com.ziqni.admin.sdk.model.Contest
import com.ziqni.transformers.domain
import org.joda.time.DateTime

import java.time.OffsetDateTime
import scala.jdk.CollectionConverters.MapHasAsScala
import scala.language.implicitConversions

case class ZiqniContest(contest: Contest) extends com.ziqni.transformers.domain.ZiqniContest with LazyLogging with ZiqniHelper {

	implicit private def offsetDateTimeToDateTime(t: OffsetDateTime): DateTime = new DateTime(t)
	implicit private def offsetDateTimeToDateTimeOpt(t: Option[OffsetDateTime]): Option[DateTime] = t.map(new DateTime(_))

	override def getName: String = contest.getName

	override def getCompetitionId: String = contest.getCompetitionId

	override def getStartDate: DateTime = contest.getScheduledStartDate

	override def getEndDate: Option[DateTime] = contest.getScheduledEndDate

	override def getCreatedTime: DateTime = contest.getCreated

	override def getContestId: String = contest.getId

	override def getProductRefIds: Option[Array[String]] = None //Option(contest.options.products.map(_.productRefId).toArray)

	override def getGroups: Option[Array[String]] = {
		//    val entList = contest.options.limitEntrantsTo
		//    val all = entList.map { eList =>
		//      eList.filter { e =>
		//        e.startsWith("$")
		//      }
		//    }
		//    val groups =
		//      if (all.nonEmpty && all.get.nonEmpty) None
		//      else contest.options.limitEntrantsTo
		//
		//    groups.map(_.toArray)
		None
	}

	override def getStatus: String = contest.getStatus.toString

	override def getMetaData: Option[Map[String, String]] = contest.getMetadata

	override def setMetaData(metadata: Map[String, String]): Unit = {
		//    val newMeta = metadata.map(mT => Metadata(mT._1, mT._2)).toArray
		//
		//    implicit val a: Account = global.domain.accounts.getById(contest.accountId.get)
		//
		//    if (contest.metadata.nonEmpty && contest.metadata.get.length == 0) {
		//      val toUpdate = contest.copy(metadata = Option(newMeta))
		//      global.domain.contests.update(toUpdate, 0L, global.core.validators.contestValidators.updateValidatorChain)
		//    }
		//    else {
		//      val currentMeta = contest.metadata.get
		//      val newMetaData = currentMeta ++ newMeta
		//      val toUpdate = contest.copy(metadata = Option(newMetaData))
		//      global.domain.contests.update(toUpdate, 0L, global.core.validators.contestValidators.updateValidatorChain)
		//    }
	}

	override def setStatus(): Unit = {
		//logger.warn(s"For accountId - [${contest.accountId}] - and contestId - [${contest.id.get}] set status to not active and create account message.")
		//TODO - do not start the contests if webhook failed.
		//TODO - create account message for after status update
		//			CreateAccountMessage.create()
	}

	override def getCustomFields: Map[String, domain.CustomFieldEntry[_]] = convertCustomFields(contest.getCustomFields.asScala.toMap)
}
