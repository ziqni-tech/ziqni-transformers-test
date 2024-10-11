package com.ziqni.transformer.test.api

import com.typesafe.scalalogging.LazyLogging
import com.ziqni.transformers.ZiqniContext.SpaceName
import com.ziqni.transformers.domain.ZiqniAccount
import com.ziqni.transformers._

import scala.concurrent.ExecutionContextExecutor
import scala.reflect.ClassTag

case class ZiqniContextExt(_accountId: String,
                           _spaceName: String,
                           _ziqniTransformerInfo: ZiqniTransformerInfo,
                           _ziqniApi: ZiqniApi,
                           _ziqniApiAsync: ZiqniApiAsync,
                           _ziqniApiHttp: ZiqniApiHttp,
                           _ziqniTransformerEventBus: Option[ZiqniTransformerEventBus],
                           _ziqniExecutionContext: ExecutionContextExecutor,
                           _ziqniSubAccounts: Seq[ZiqniAccount],
                           _ziqniSubAccountApiAsync: SpaceName => ZiqniApiAsync
                          ) extends com.ziqni.transformers.ZiqniContext with LazyLogging {
  override def accountId: String = _accountId

  override def spaceName: String = _spaceName

  override def ziqniTransformerInfo: ZiqniTransformerInfo = _ziqniTransformerInfo

  override def ziqniApi: ZiqniApi = _ziqniApi

  override def ziqniApiAsync: ZiqniApiAsync = _ziqniApiAsync

  override def ziqniApiHttp: ZiqniApiHttp = _ziqniApiHttp

  override def ziqniTransformerEventBus: Option[ZiqniTransformerEventBus] = _ziqniTransformerEventBus

  override def ziqniExecutionContext: ExecutionContextExecutor = _ziqniExecutionContext

  override def ziqniSubAccounts: Seq[ZiqniAccount] = _ziqniSubAccounts

  override def ziqniSubAccountApiAsync(spaceName: SpaceName): ZiqniApiAsync = _ziqniSubAccountApiAsync.apply(spaceName)

  override def ziqniConnectionParameterKeys(): Set[String] = Set.empty

  override def ziqniConnectionParameter(connectionParameterKey: String): Option[AnyRef] = None

  override def ziqniSystemLogWriter(message: String, throwable: Throwable, logLevel: LogLevel): Unit = {
    if(LogLevel.INFO == logLevel)
      logger.info(message, throwable)
    else if (LogLevel.WARN == logLevel)
      logger.warn(message, throwable)
    else if (LogLevel.DEBUG == logLevel)
      logger.debug(message, throwable)
    else if (LogLevel.ERROR== logLevel)
      logger.error(message, throwable)
    else if (LogLevel.TRACE == logLevel)
      logger.trace(message, throwable)
  }

  override def $[TZ](t: String)(implicit ct: ClassTag[TZ]): TZ = ???
}
