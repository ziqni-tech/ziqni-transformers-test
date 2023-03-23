///*
// * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
// */
//
//package it.sisal.live
//
//import com.ziqni.transformers.domain.WebhookSettings
//import it.domain.MockAwardModel
//import it.utils.ZiqniApiTest
//import org.scalatest._
//import org.scalatest.funspec.AnyFunSpec
//import org.scalatest.matchers.must.Matchers
//
//class SisalWebhookTransTest extends AnyFunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach with BeforeAndAfterAll {
//
//	describe("Test the webhook implementation") {
//
//		it("should receive an retrieve auth code from remote system when webhooks are fired") {
//
//			val api = new ZiqniApiTest()
//
//			// Prepare the test
//			val webhook = new SisalWebhookTrans()
//			val settings = new WebhookSettings() {
//				override def url = ""
//				override def headers: Map[String, Seq[String]] = Map.empty
//			}
//
//			val memberId = "123123"
//			val contestId = "contestId"
//			api.memberForTest.put(memberId, memberId)
//			api.contestData.put(contestId, api.newContest)
//
//			webhook.onContestRewardIssued(settings, contestId, memberId, MockAwardModel.AwardIdForAchievements, "real-bonus", api)
//
//			assert(true)
//		}
//	}
//}