package com.github.ivsokol.poe.catalog

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ExtensionsKtTest :
    DescribeSpec({
      describe("anyOfAny") {
        it("should return true if any of the elements is in the other list") {
          listOf("a", "b", "c").anyOfAny(listOf("b", "d")) shouldBe true
        }
        it("should return true if any of the elements is in the other list, reverse lists") {
          listOf("b", "d").anyOfAny(listOf("a", "b", "c")) shouldBe true
        }
        it("should return false if none of the elements is in the other list") {
          listOf("a", "b", "c").anyOfAny(listOf("d", "e")) shouldBe false
        }
      }

      describe("allOfAll") {
        it("should return true if all of the elements are in the other list") {
          listOf("a", "b", "c").allOfAll(listOf("a", "b")) shouldBe true
        }
        it("should return false if any of the elements is not in the other list") {
          listOf("a", "b", "c").allOfAll(listOf("a", "d")) shouldBe false
        }
      }
    })
