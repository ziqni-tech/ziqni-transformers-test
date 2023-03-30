package com.ziqni.transformer.test.api

import com.ziqni.transformers.ZiqniApiHttp
import com.ziqni.transformers.domain.{BasicAuthCredentials, HttpResponseEntity}
import sttp.client.{UriContext, basicRequest}
import sttp.client._
import sttp.client.quick.backend
import sttp.model.Method

object DefaultZiqniApiHttp extends ZiqniApiHttp {

  override def httpGet(url: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {
    val request = basicRequest.method(Method.GET, uri"$url")

    sendAndSetResult(setHeaders(request, headers))
  }

  override def httpPut(url: String, body: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {
    val request = basicRequest
      .method(Method.PUT, uri"$url")
      .body(body)
      .contentType("application/json")
//      .acceptEncoding("gzip, deflate")

    sendAndSetResult(setHeaders(request, headers))
  }

  override def httpPost(url: String, body: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {


    val request = basicRequest.method(Method.POST, uri"$url")
      .body(body)
      .contentType("application/json")
//      .acceptEncoding("gzip, deflate")

    sendAndSetResult(setHeaders(request, headers))
  }

  override def httpDelete(url: String, headers: Map[String, Seq[String]], basicAuthCredentials: Option[BasicAuthCredentials], sendCompressed: Boolean = true): HttpResponseEntity = {
    val request = basicRequest.method(Method.DELETE, uri"$url")

    sendAndSetResult(setHeaders(request, headers))
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

  private def sendAndSetResult(request: Request[Either[String, String], Nothing]): HttpResponseEntity = {
    val result = request.send()
    result.body.fold(
      ko =>
        HttpResponseEntity(ko, result.code.code),
      ok =>
        HttpResponseEntity(ok, result.code.code)
    )
  }
}
