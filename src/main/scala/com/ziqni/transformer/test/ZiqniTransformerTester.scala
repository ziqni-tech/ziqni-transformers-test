package com.ziqni.transformer.test

import com.ziqni.transformer.test.api.{DefaultZiqniApiHttp, ZiqniApiAsyncClient, ZiqniApiClient, ZiqniContextExt}
import com.ziqni.transformer.test.store.ZiqniStores
import com.ziqni.transformers.ZiqniTransformerInfo
import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.TrieMap

object ZiqniTransformerTester {

  val TestAccountId = "TEST_ACCOUNT_FwNUW3M"
  val TestSpaceName = "my-test-space"

  def loadDefault(): ZiqniTransformerTester = {
    val ziqniStores = new ZiqniStores(TestAccountId)
    val ziqniApiAsync = ZiqniApiAsyncClient(ziqniStores = ziqniStores, masterAccount = None, accountId = TestAccountId, spaceName = TestSpaceName, _subAccounts = TrieMap.empty, actions = new ConcurrentHashMap[String, Seq[String]]())
    val ziqniApi = ZiqniApiClient(async = ziqniApiAsync, masterAccount = None, accountId = TestAccountId, spaceName = TestSpaceName, _subAccounts = TrieMap.empty, actions = new ConcurrentHashMap[String, Seq[String]]())
    val context = ZiqniContextExt(
      TestAccountId,
      TestSpaceName,
      ZiqniTransformerInfo("TEST_CONNECTION_UW3M", "my-test-connection", "TEST_TRANSFORMER_UW3"),
      ziqniApi,
      ziqniApiAsync,
      DefaultZiqniApiHttp,
      None,
      ZiqniApiClient.GlobalZiqniApiClientContext,
      Seq.empty,
      _ => null
    )

    ZiqniTransformerTester(context, ziqniStores)
  }

  def loadDefaultWithSampleData(): ZiqniTransformerTester = {
    val out = loadDefault()
    out.ziqniStores.generateSampleData()
    out
  }
}

case class ZiqniTransformerTester(ziqniContextExt: ZiqniContextExt, ziqniStores: ZiqniStores){

}