package com.ziqni.transformer.test.api

import com.ziqni.transformers.ZiqniContext.SpaceName
import com.ziqni.transformers.domain.ZiqniAccount
import com.ziqni.transformers._

import scala.concurrent.ExecutionContextExecutor

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
                          ) extends com.ziqni.transformers.ZiqniContext {
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

  override def ziqniConnectionParameterKeys(): Set[String] = ???

  override def ziqniConnectionParameter(connectionParameterKey: String): Option[AnyRef] = ???

  override def ziqniSystemLogWriter(message: String, throwable: Throwable, logLevel: LogLevel): Unit = ???
}
