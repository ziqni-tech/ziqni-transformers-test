package com.ziqni.transformer.test.models

import com.typesafe.scalalogging.LazyLogging
import com.ziqni.admin.sdk.model._
import com.ziqni.transformers.domain.BasicAchievementModel
import org.joda.time.DateTime

import java.time.OffsetDateTime
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

case class BasicAchievement(achievement: Achievement) extends BasicAchievementModel with LazyLogging with BasicModelHelper {
	implicit private def stringToOption(s: String): Option[String] = Option(s)
	implicit private def offsetDateTimeToDateTime(t: OffsetDateTime): DateTime = new DateTime(t)
	implicit private def offsetDateTimeToDateTimeOpt(t: Option[OffsetDateTime]): Option[DateTime] = t.map(new DateTime(_))

	override def getName: String = achievement.getName

	override def getDescription: Option[String] = achievement.getDescription

	override def isDeprecated: Boolean = achievement.getConstraints.contains("deprecated")

	override def getStartDate: DateTime = Option(achievement.getScheduling.getStartDate).map(t => new DateTime(t.toInstant.toEpochMilli)).getOrElse(DateTime.now())

	override def getEndDate: Option[DateTime] = Option(achievement.getScheduling.getEndDate).map(t => new DateTime(t.toInstant.toEpochMilli))

	override def getCategory: Option[Array[String]] = None //achievement.category.map(_.toArray)

	override def getCreatedTime: DateTime = Option(achievement.getCreated).map(t =>new DateTime(t.toInstant.toEpochMilli)).getOrElse(DateTime.now())

	override def getClAchievementId: String = achievement.getId

	//TODO - clean me - this is BAD!
	override def getProductRefIds: Option[Array[String]] = {
		//    val y = achievement.ruleSets.map(r => {
		//      r.conditions.map(m => {
		//        m.rules.map(mr => {
		//          mr.getSubConditions.map(sb => {
		//            sb.flatMap(sb1 => {
		//              val a = sb1.subRules.filter(_.fact == "event.product.ref.id").map(_.constant)
		//
		//              def b: Array[String] = sb1.subRules.filter(_.fact == "event.product.id").map(_.constant)
		//
		//              if (a.nonEmpty)
		//                a
		//              else if (b.nonEmpty) {
		//                val aa = b.map(z => {
		//                  global.core.findProductsBy.productRef(z).get
		//                })
		//                aa
		//              } else
		//                None
		//
		//            })
		//          })
		//        })
		//      })
		//    })
		//    val z = y.flatten
		//    Option(z)
		None
	}

	override def getGroups: Option[Array[String]] = {
		//    Option(achievement.getMemberGroups).map( groups => groups.m)
		None
	}

	override def getStatus(): String = achievement.getStatus.getValue

	override def getMetaData: Option[Map[String, String]] = achievement.getMetadata

	override def setMetaData(metadata: Map[String, String]): Unit = {

		val result = mergeMetaData(achievement.getMetadata, metadata, merged => {
			achievement.metadata(merged.asJava)
		})

		// todo - Call API update achievement
	}

	override def setStatus(): Unit = {
		//    achievement.achievementLiveStatus.foreach(status => {
		//      if (status == AchievementLiveStatus.Live.toString) {
		//        val toUpdate = achievement.copy(achievementLiveStatus = AchievementLiveStatus.Draft.toString)
		//        implicit val a: Account = global.domain.accounts.getById(achievement.accountId.get)
		//        logger.warn(s"For accountId - [${achievement.accountId.get}] - and achievementId - [${achievement.id.get}] set status to [DRAFT].")
		//        global.domain.achievements.update(toUpdate, 0L, global.core.validators.achievementValidators.updateValidatorChain, Vector.empty)
		//      }
		//    })
	}
}
