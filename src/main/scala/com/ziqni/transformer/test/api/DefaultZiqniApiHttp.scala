package com.ziqni.transformer.test.api

import com.ziqni.transformers.{ZiqniApiHttp, ZiqniContext}
import com.ziqni.transformers.domain.{ZiqniAuthCredentials, HttpResponseEntity}
import sttp.client.{UriContext, basicRequest, _}
import sttp.client.quick.backend
import sttp.model.Method

object DefaultZiqniApiHttp extends com.ziqni.transformers.ZiqniApiHttp {

  override def httpGetWithLogMessage(url: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[ZiqniAuthCredentials], sendCompressed: Boolean = true, logMessage: Option[String], ziqniContext: ZiqniContext): HttpResponseEntity = {
    val request = basicRequest.method(Method.GET, uri"$url")

    basicAuthCredentials.foreach(s => request.auth.basic(s.username,s.password))

    sendAndSetResult(setHeaders(request, headers), logMessage, ziqniContext)
  }

  override def httpPutWithLogMessage(url: String, body: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[ZiqniAuthCredentials], sendCompressed: Boolean = true, logMessage: Option[String], ziqniContext: ZiqniContext): HttpResponseEntity = {
    val request = basicRequest
      .method(Method.PUT, uri"$url")
      .body(body)
      .contentType("application/json")

    basicAuthCredentials.foreach(s => request.auth.basic(s.username,s.password))

    sendAndSetResult(setHeaders(request, headers), logMessage, ziqniContext)
  }

  override def httpPostWithLogMessage(url: String, body: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[ZiqniAuthCredentials], sendCompressed: Boolean = true, logMessage: Option[String], ziqniContext: ZiqniContext): HttpResponseEntity = {


    val request = basicRequest.method(Method.POST, uri"$url")
      .body(body)
      .contentType("application/json")

    basicAuthCredentials.foreach(s => request.auth.basic(s.username,s.password))

    sendAndSetResult(setHeaders(request, headers), logMessage, ziqniContext)
  }

  override def httpDeleteWithLogMessage(url: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[ZiqniAuthCredentials], sendCompressed: Boolean = true, logMessage: Option[String], ziqniContext: ZiqniContext): HttpResponseEntity = {
    val request = basicRequest.method(Method.DELETE, uri"$url")

    basicAuthCredentials.foreach(s => request.auth.basic(s.username,s.password))

    sendAndSetResult(setHeaders(request, headers), logMessage, ziqniContext)
  }

  private def setHeaders(request: Request[Either[String, String], Nothing], headers: Map[String, Seq[String]]): Request[Either[String, String], Nothing] = {
    val headersFlat = new scala.collection.mutable.HashMap[String, String]()

    headers.foreach(header =>
      if (header._1.equalsIgnoreCase("Content-Type")
        || header._1.equalsIgnoreCase("Content-Encoding")) {
      }
      else {
        header._2.foreach(
          headersFlat.put(header._1,_)
        )
      }
    )

    request.headers(headersFlat.toMap)
  }

  def sendAndSetResult(request: Request[Either[String, String], Nothing], logMessage: Option[String], ziqniContext: ZiqniContext): HttpResponseEntity = {
    val result = request.send()

    val mesg = logMessage.map(m => ", "+m.take(256)).getOrElse("")

    result.body.fold(
      ko => {
        HttpResponseEntity(ko, result.code.code)
      },
      ok => {
        HttpResponseEntity(ok, result.code.code)
      }
    )
  }
}
