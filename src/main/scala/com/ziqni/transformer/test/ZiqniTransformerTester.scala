package com.ziqni.transformer.test

import com.ziqni.transformer.test.api.{DefaultZiqniApiHttp, ZiqniApiAsyncClient, ZiqniApiClient, ZiqniContextExt}
import com.ziqni.transformer.test.concurrent.ZiqniExecutors
import com.ziqni.transformer.test.store.ZiqniStores
import com.ziqni.transformers.ZiqniTransformerInfo

object ZiqniTransformerTester {

  val TestAccountId = "TEST_ACCOUNT_FwNUW3M"
  val TestSpaceName = "my-test-space"

  def loadDefault(): ZiqniTransformerTester = {
    var ziqniStores = new ZiqniStores(TestAccountId)
    val ziqniApiAsync = ZiqniApiAsyncClient(ziqniStores = ???, masterAccount = ???, accountId = ???, spaceName = ???, _subAccounts = ???, actions = ???)
    val ziqniApi = ZiqniApiClient(async = ziqniApiAsync, masterAccount = ???, accountId = ???, spaceName = ???, _subAccounts = ???, actions = ???)

    ZiqniTransformerTester(
      new ZiqniContextExt(
        TestAccountId,
        TestSpaceName,
        ZiqniTransformerInfo("TEST_CONNECTION_UW3M", "my-test-connection","TEST_TRANSFORMER_UW3"),
        ziqniApi,
        ziqniApiAsync,
        DefaultZiqniApiHttp,
        None,
        ZiqniApiClient.GlobalZiqniApiClientContext,
        Seq.empty,
        _ => null
      ),
      ziqniStores
      )
  }
}

case class ZiqniTransformerTester(ziqniContextExt: ZiqniContextExt, ziqniStores: ZiqniStores){

}