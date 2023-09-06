package com.ziqni.transformer.test.utils

import java.util.concurrent.CompletionStage
import scala.concurrent.Future
import scala.jdk.FutureConverters.FutureOps

object JavaUtils {

  def scalaFutureToJava[T1](in: Future[T1]): CompletionStage[T1] = in.asJava

}
