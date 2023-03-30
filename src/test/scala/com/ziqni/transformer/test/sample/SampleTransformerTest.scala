/*
 * Copyright (c) 2022. ZIQNI LTD registered in England and Wales, company registration number-09693684
 */

package com.ziqni.transformer.test.sample

import com.ziqni.transformer.test.ZiqniTransformerTester
import com.ziqni.transformer.test.mocks.ExampleTransformerImpl
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

class SampleTransformerTest extends AnyFunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach with BeforeAndAfterAll {

	describe("Test the message queue receiver implementation") {

		it("should receive a published message and transform it into an event") {

      val ziqniTransformerTester = ZiqniTransformerTester.loadDefault()
			val transformer = new ExampleTransformerImpl
			transformer.rabbit(Array.empty, "","", ziqniTransformerTester.ziqniContextExt)

		}
	}
}



