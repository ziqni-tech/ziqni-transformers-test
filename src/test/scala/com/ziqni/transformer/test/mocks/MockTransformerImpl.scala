package com.ziqni.transformer.test.mocks

import com.typesafe.scalalogging.LazyLogging
import com.ziqni.transformers.domain.{BasicEventModel, BasicProductModel, CustomFieldEntry}
import com.ziqni.transformers.{Json, ZiqniContext, ZiqniMqTransformer}
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject
import org.json4s.jackson.parseJson
import org.json4s.{DefaultFormats, JArray, JValue}

import scala.collection.immutable.{Map, Seq}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

class MockTransformerImpl extends ZiqniMqTransformer with LazyLogging {
  private implicit val formats: DefaultFormats.type = DefaultFormats
  private val SystemMemberRef = "system"
  private val TIER_PREFIX = "Tier"

  private def getSpaceFromTenantId(tenantId: Int): String = tenantId match {
    case 1 => "bol"
    case 7 => "bol"
    case 6 => "wcasino"
    case _ => throw new Exception(s"The Space for TenantId:$tenantId does not exist.")
  }


  override def apply(message: Array[Byte], ziqniContext: ZiqniContext, args: Map[String, Any]): Unit = {
    throw new NotImplementedError("This transformer is specific to RabbitAMPQ")
  }

  override def rabbit(message: Array[Byte], routingKey: String, exchangeName: String, ziqniContext: ZiqniContext): Unit = {
    implicit val z: ZiqniContext = ziqniContext
    implicit val ze: ExecutionContextExecutor = ziqniContext.ziqniExecutionContext

    val messageAsString = ZiqniContext.convertByteArrayToString(message)
    val jsonObj = ZiqniContext.fromJsonString(messageAsString)

    if (routingKey.endsWith("-transactions")) {
      handleTransaction(jsonObj)
    }
    else if (routingKey.endsWith("-member")) {
      handleMember(jsonObj)
    }
    else if (routingKey == "products-update") {
      handleProduct(messageAsString)
    }
    else if (routingKey == "products-delete") {
      handleDeleteProduct(messageAsString)
    }
    else {
      throw new NotImplementedError(s"No handler implemented for routing key [$routingKey]")
    }
  }

  /// BEGIN: HANDLE PRODUCT ///

  /**
   * https://transform.tools/json-to-scala-case-class
   *
   * @param subAccountCompetitionLabsApi
   * @param productId
   * @param gameId
   * @param timestamp
   * @param ec
   */
  private def handleProduct(messageAsString: String)(implicit ziqniContext: ZiqniContext, ec: ExecutionContext): Unit = {
    val jsValue = parseJson(messageAsString)
    val productMessage = jsValue.extract[ProductMessage]
    onNewGenericGameCatalogChanged(productMessage)(ziqniContext, ziqniContext.ziqniExecutionContext)
  }

  private def onNewGenericGameCatalogChanged(productMessage: ProductMessage)(implicit ziqniContext: ZiqniContext, ec: ExecutionContext): Unit = {

    val transactionTimestamp = new DateTime(productMessage.time)
    val metadata = Map(
      "lastUpdate" -> transactionTimestamp.toString()
    )

    getProductIdByGameId(productMessage.data.gameId.toString) match {
      case Some(product) =>
        val metadataExist = product.getMetaData
        val lastUpdateAsString = metadataExist.get("lastUpdate")
        val lastUpdate = new DateTime(lastUpdateAsString)

        // If the update was made after the last update the product will be updated
        if (transactionTimestamp.isAfter(lastUpdate))
          updateProduct(ziqniContext, product.getClProductId, productMessage.data.gameId.toString, productMessage.data.name, Array(productMessage.data.provider), productMessage.data.gameType, metadata)(ziqniContext.ziqniExecutionContext)

      case _ =>
        createProduct(productMessage, metadata)
    }
  }


  private def createProduct(productMessage: ProductMessage, metadata: Map[String, String])(implicit ziqniContext: ZiqniContext, ec: ExecutionContext): Unit = {
    val eventMetadata: Map[String, CustomFieldEntry[Any]] = Map(
      "gameId" -> productMessage.data.gameId,
      "name" -> productMessage.data.name,
      "gameType" -> productMessage.data.gameType
    )
    try {
      ziqniContext.ziqniApiAsync.createProduct(productMessage.data.gameId.toString, productMessage.data.name, Seq(productMessage.data.provider), productMessage.data.gameType, 1, Option(metadata))
        .onComplete {

          case Failure(exception) =>
            logger.error("Failed", exception.getMessage)
            val basicEvent = createEvent("product.create.failed", DateTime.now(), productMessage.data.gameId.toString, SystemMemberRef, eventMetadata)
            ziqniContext.ziqniApiAsync.pushEvent(basicEvent)

          case Success(result) =>
            if (result.isEmpty) {
              logger.error("Failed to commit ")
            }
        }

    } catch {
      case e: Exception =>
        val basicEvent = createEvent("product.create.failed", DateTime.now(), productMessage.data.gameId.toString, SystemMemberRef, eventMetadata)
        ziqniContext.ziqniApiAsync.pushEvent(basicEvent)
        throw e
    }
  }

  private def getProductIdByGameId(gameId: String)(implicit ziqniContext: ZiqniContext, ec: ExecutionContext): Option[BasicProductModel] = {
    try {
      // Try to get the productId from ZIQNI database
      ziqniContext.ziqniApi.productIdFromProductRefId(gameId).flatMap(ziqniContext.ziqniApi.getProduct)
    } catch {
      case e: Exception =>
        logger.error(s" Failed to process gameId: [$gameId]", e)
        None
    }
  }

  private def updateProduct(ziqniContext: ZiqniContext, productId: String, gameId: String, name: String, provider: Array[String], gameType: String, metadata: Map[String, String])(implicit ec: ExecutionContext): Unit = {
    val eventMetadata: Map[String, CustomFieldEntry[Any]] = Map(
      "gameId" -> gameId,
      "name" -> name,
      "gameType" -> gameType
    )
    try {
      ziqniContext.ziqniApiAsync.updateProduct(
        productId,
        Option(gameId),
        Option(name),
        Option(provider),
        Option(gameType),
        Option(1),
        Option(metadata)
      )
        .onComplete {

          case Failure(exception) =>
            logger.error("Failed", exception.getMessage)
            val basicEvent = createEvent("product.update.failed", DateTime.now(), gameId, SystemMemberRef, eventMetadata)
            ziqniContext.ziqniApiAsync.pushEvent(basicEvent)
          case Success(result) =>
            val basicEvent = createEvent("product.updated", DateTime.now(), gameId, SystemMemberRef, eventMetadata)
            ziqniContext.ziqniApiAsync.pushEvent(basicEvent)
        }
    } catch {
      case e: Exception =>
        // if product update failed create a failed event
        val basicEvent = createEvent("product.update.failed", DateTime.now(), gameId, SystemMemberRef, eventMetadata)
        ziqniContext.ziqniApiAsync.pushEvent(basicEvent)
        throw e
    }
  }

  //Objects
  case class ProductData(
                          gameId: Int,
                          name: String,
                          gameType: String,
                          gameSubtype: String,
                          provider: String
                        )

  private case class ProductMessage(specversion: String,
                                    id: String,
                                    source: String,
                                    `type`: String,
                                    datacontenttype: String,
                                    time: String,
                                    data: ProductData)


  private def handleDeleteProduct(messageAsString: String)(implicit ziqniContext: ZiqniContext, ec: ExecutionContext): Unit = {

    try {
      val jsonObj = ZiqniContext.fromJsonString(messageAsString)
      val messageType = Json.getFromJValue[String](jsonObj, "type")

      val transactionTimestamp = new DateTime(Json.getFromJValue[String](jsonObj, "time"))
      val productObj = Json.getFromJValue[JObject](jsonObj, "data")
      val gameId = Json.getFromJValue[String](productObj, "gameId")
      val metadata = Map("lastUpdate" -> transactionTimestamp.toString())

      // Try to get the productId from ZIQNI database
      ziqniContext.ziqniApiAsync.productIdFromProductRefId(gameId).onComplete {
        case Failure(exception) => logger.error("Failed", exception.getMessage)
        case Success(productIdAsOption) => {
          if (productIdAsOption.nonEmpty)
            deleteProduct(productIdAsOption.get, gameId, metadata)
        }
      }
    }
    catch {
      case e: Exception =>
        logger.error(s"[${e.getMessage}] - Exception occurred for message: [$messageAsString]", e)
    }
  }

  private def deleteProduct(productId: String, gameId: String, metadata: Map[String, String])(implicit ziqniContext: ZiqniContext, ec: ExecutionContext): Unit =
    getProductIdByGameId(gameId) match {
      case Some(product) =>
        val updatedName = "Marked-For-Deletion-" + product.getName
        updateProduct(ziqniContext, productId, gameId, updatedName, product.getProviders, product.getProductType, metadata)
      case _ =>
        logger.error("Failed to find " + productId)
    }

  /// END: HANDLE PRODUCT ///

  /// BEGIN: HANDLE MEMBER ///
  private def handleMember(jsonObj: JValue)(implicit ziqniContext: ZiqniContext, executionContext: ExecutionContextExecutor): Unit = {


    val transactionTimestamp = new DateTime(Json.getFromJValue[String](jsonObj, "time"))
    val customerObj = Json.getFromJValue[JObject](jsonObj, "data")
    val customerId = Json.getFromJValue[String](customerObj, "customerId")
    val alias = Json.getFromJValueAsOption[String](customerObj, "alias").getOrElse(customerId)
    val tenantId = Json.getFromJValue[Int](customerObj, "tenantId")
    val customerTier = Json.getFromJValue[String](customerObj, "customerTier")
    val customerTierPeak = Json.getFromJValue[String](customerObj, "customerTierPeak")

    val metadata = Map(
      "lastUpdate" -> transactionTimestamp.toString(),
      "tenantId" -> tenantId.toString
    )

    val space = getSpaceFromTenantId(tenantId)
    val groups = getGroupsFromCustomerTiers(customerTier, customerTierPeak, space)

    ziqniContext.ziqniApi.memberIdFromMemberRefId(customerId) match {
      case Some(memberId) =>
        val member = ziqniContext.ziqniApi.getMember(memberId).get
        val metadataExist = member.getMetaData
        val lastUpdateAsString = metadataExist.get("lastUpdate")
        val lastUpdate = new DateTime(lastUpdateAsString)

        if (transactionTimestamp.isAfter(lastUpdate)) {
          updateMember(memberId, customerId, alias, groups, metadata, transactionTimestamp)(ziqniContext, ziqniContext.ziqniExecutionContext)
        }
      case _ =>
        createMember(customerId, alias, groups, metadata, transactionTimestamp)(ziqniContext, ziqniContext.ziqniExecutionContext)
    }
  }

  private def createMember(customerId: String, alias: String, groups: Seq[String], metadata: Map[String, String],
                           transactionTimestamp: DateTime)(implicit ziqniContext: ZiqniContext, executionContext: ExecutionContextExecutor): Unit = {
    try {
      ziqniContext.ziqniApiAsync.createMember(customerId, alias, groups, Option(metadata))
        .onComplete {

          case Failure(exception) =>
            logger.error("Failed", exception.getMessage)
          case Success(result) =>
            if (result.isEmpty) {
              logger.error("Failed to commit ")
            }
        }
    } catch {
      case e: Exception =>
        // if member create failed create a failed event
        ziqniContext.ziqniApiAsync.pushEvent(createEvent("member.create.failed", transactionTimestamp, "system", customerId))
        throw e
    }
  }

  private def updateMember(memberId: String, customerId: String, alias: String, groups: Seq[String], metadata: Map[String, String], transactionTimestamp: DateTime)(ziqniContext: ZiqniContext, executionContext: ExecutionContextExecutor): Unit = {
    try {
      ziqniContext.ziqniApi.updateMember(memberId, Option(customerId), Option(alias), Option(groups), Option(metadata))
    } catch {
      case e: Exception =>
        // if member update failed create a failed event
        ziqniContext.ziqniApi.pushEvent(createEvent("member.update.failed", transactionTimestamp, "system", customerId))
        throw e
    }
  }

  private def getGroupsFromCustomerTiers(customerTier: String, customerTierPeak: String, space: String): Seq[String] = {
    Seq(
      buildGroupFromCustomerTier(customerTier, ""),
      buildGroupFromCustomerTier(customerTierPeak, "Peak"),
      space
    )
  }

  private def buildGroupFromCustomerTier(customerTier: String, suffix: String): String = {
    customerTier match {
      case group if group.contains(TIER_PREFIX) => s"${group.replaceAll(" ", "")}$suffix"
      case _ => s"$TIER_PREFIX${customerTier.replaceAll("/", "")}$suffix"
    }
  }
  /// END: HANDLE MEMBER ///

  /// BEGIN: HANDLE TRANSACTION ///

  // Helpers

  private def handleTransaction(jsonObj: JValue)(implicit ziqniContext: ZiqniContext): Unit = {
    val dataObj = Json.getFromJValue[JObject](jsonObj, "data")
    val tenantId = Json.getFromJValue[Int](dataObj, "tenantId")
    val roundId = Json.getFromJValue[String](dataObj, "roundId")
    val productId = Json.getFromJValue[Int](dataObj, "gameId")
    val memberId = Json.getFromJValue[String](dataObj, "customerId")
    val transactionsObj = Json.getFromJValue[JArray](dataObj, "transactions")
    val (totalDebit, winMultiplier, biggerDate) = calculateWinMultiplier(transactionsObj)
    sendEvent(winMultiplier, totalDebit, memberId, productId.toString, roundId, biggerDate)(ziqniContext, ziqniContext.ziqniExecutionContext)
  }

  def calculateWinMultiplier(transaction: JArray): (Double, Double, DateTime) = {
    var debit, credit: Double = 0
    var biggerDate: DateTime = null
    transaction.arr.foreach { jsonObj =>

      val transactionType = Json.getFromJValue[Int](jsonObj, "typeId")
      val transactionAmount = Json.getFromJValue[Int](jsonObj, "amount")
      val transactionDate = new DateTime(Json.getFromJValue[String](jsonObj, "dateTime"))

      // 1 = debit, 2 = credit
      if (transactionType == 1) {
        debit = debit + transactionAmount
      } else if (transactionType == 2) { //this means debit = 0
        credit = credit + transactionAmount
      }

      if (biggerDate == null || transactionDate.isAfter(biggerDate)) {
        biggerDate = transactionDate;
      }
    }

    val winMultiplier = if (debit == 0) 0 else credit / debit

    (debit, winMultiplier, biggerDate)
  }

  private def sendEvent(winMultiplier: Double, debit: Double,
                        memberId: String, productId: String,
                        roundId: String, biggerDate: DateTime)(implicit ziqniContext: ZiqniContext, executionContext: ExecutionContext): Unit = {

    val actionWinMultiplier = "win-multiplier"
    val customFieldBetAmount = "bet-amount"

    doActionExists(
      actionWinMultiplier,
      () => {
        ziqniContext.ziqniApiAsync.pushEvent(
          BasicEventModel(
            memberId = None,
            eventRefId = roundId,
            memberRefId = memberId,
            entityRefId = productId,
            action = actionWinMultiplier,
            sourceValue = winMultiplier,
            transactionTimestamp = biggerDate,
            batchId = Option(roundId),
            customFields = Map(
              "win-multiplier" -> actionWinMultiplier,
              "bet-amount" -> customFieldBetAmount
            ),
            metadata = Map.empty
          ))
      }
    )

  }

  /**
   * Check if the specific action exists. If not, create that action.
   */
  private def doActionExists(action: String, next: () => Unit)(implicit ziqniContext: ZiqniContext, executionContext: ExecutionContext): Unit = {
    ziqniContext.ziqniApiAsync.eventActionExists(action).onComplete {
      case Failure(exception) =>
        throw (exception)
      case Success(eventActionExists) =>
        if (!eventActionExists) {
          ziqniContext.ziqniApiAsync.createEventAction(action, None, None, None)
        }
        next.apply()
    }
  }

  /// END: HANDLE TRANSACTION ///

  // HELPERS ///

  implicit def int2string(i: Int): String = i.toString

  private def createEvent(action: String, timestamp: DateTime, productRefId: String, memberRefId: String, customFields: Map[String, CustomFieldEntry[Any]] = Map.empty): BasicEventModel = {
    BasicEventModel(
      memberId = None,
      action = action,
      tags = Seq.empty,
      eventRefId = timestamp.toString(),
      memberRefId = memberRefId,
      entityRefId = productRefId,
      batchId = None,
      sourceValue = 0,
      metadata = Map.empty,
      customFields = customFields,
      transactionTimestamp = timestamp
    )
  }

  private implicit def toCustomFieldEntry(s: String): CustomFieldEntry[Any] = new CustomFieldEntry[Any]("Text", s)

  private implicit def toCustomFieldEntry(s: Array[String]): CustomFieldEntry[Any] = new CustomFieldEntry[Any]("TextArray", s)

  private implicit def toCustomFieldEntry(s: Boolean): CustomFieldEntry[Any] = new CustomFieldEntry[Any]("Text", s)

  private implicit def toCustomFieldEntry(s: Int): CustomFieldEntry[Any] = new CustomFieldEntry[Any]("Number", s)
}
