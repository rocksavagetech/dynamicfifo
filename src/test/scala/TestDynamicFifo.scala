// This confidential and proprietary software may be used only as authorized by
// Rocksavage Technology, Inc. In the event of publication the following
// notice is applicable:
//
// (C) COPYRIGHT 2021 ROCKSAVAGE TECHNOLOGY, INC.
// ALL RIGHTS RESERVED
//
// The entire notice must be reproduced on all authorized copies.
//
// File         : TestDynamicFifo.scala
// Author       : Warren Savage
// Abstract     : Set of regression tests for DynamicFifo
//
// MODIFICATION HISTORY
// ============================================================================
// Date         Version   By                Description
// ----------------------------------------------------------------------------
// 2023-09-29   0.1       Warren Savage     Initial version
//
// ============================================================================

package chiselWare

import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.coverage._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import firrtl2.options.TargetDirAnnotation
import scala.util.Random
import scala.math.pow
import scala.collection.mutable.Stack

class TestDynamicFifo
    extends AnyFlatSpec
    with Matchers
    with ChiselScalatestTester {

  val numTests = 50
  val verbose  = false

  // Main test code defined in the form of a function that can be repeatedly
  // called and each having a randomized configuration
  def main(testName: String): Unit = {

    behavior of testName

    val backendAnnotations = Seq(
      // WriteVcdAnnotation,
      // WriteFstAnnotation,
      VerilatorBackendAnnotation,
      // IcarusBackendAnnotation,
      // VcsBackendAnnotation,
      TargetDirAnnotation("generated")
    )

    // Randomize input variables
    val validDataWidths = Seq(4, 5, 6, 7, 8, 16, 32, 64) // , 128)
    val validFifoDepths = Seq(4, 8, 16) // , 32, 64,128 ,256, 512,1024, 2048)
    val dataWidth      = validDataWidths(Random.nextInt(validDataWidths.length))
    val fifoDepth      = validFifoDepths(Random.nextInt(validFifoDepths.length))
    val useExternalRam = (fifoDepth * dataWidth) > 1024
    val testDataSize   = fifoDepth * Random.between(10, 20)
    val fullThreshold =
      (fifoDepth * Random.between(75, 95) / 100).toInt
    val emptyThreshold =
      (fifoDepth * Random.between(3, 25) / 100).toInt

    val myParams = BaseParams(
      dataWidth,
      fifoDepth,
      useExternalRam,
      bbFiles = List("dual_port_sync_sram"),
      coverage = true
    )

    it should "test this configuration" in {
      info(s"Test Data Size = $testDataSize")
      info(s"Data Width = $dataWidth")
      info(s"FIFO Depth = $fifoDepth")
      info(s"Almost Full Threshold = $fullThreshold")
      info(s"Almost Empty Threshold = $emptyThreshold")
      info("--------------------------------")
      val cov = test(new DynamicFifo(myParams))
        .withAnnotations(backendAnnotations) { dut =>
          dut.clock.setTimeout(0)

          def push(pushData: UInt): Unit = {
            dut.io.push.poke(true.B)
            dut.io.dataIn.poke(pushData)
            val pushedData = dut.io.dataIn.peek().litValue.toString
            if (verbose) { println(s"pushed $pushedData") }
            dut.clock.step()
            dut.io.push.poke(false.B)
          }

          def pop(expectedData: UInt): Unit = {
            dut.io.pop.poke(true.B)
            val poppedData = dut.io.dataOut.peek().litValue.toString
            if (verbose) { println(s"\t\t popped $poppedData") }
            dut.io.dataOut.expect(expectedData)
            dut.clock.step()
            dut.io.pop.poke(false.B)
          }

          def isEmpty(): Boolean = { return dut.io.empty.peek().litValue == 1 }
          def isFull(): Boolean  = { return dut.io.full.peek().litValue == 1 }

          // Create an independent method for checking the correct status of
          // the hardware flags. We use a Scala Stack to do this.
          var stack = Stack[Int]()
          def checkFlags(): Unit = {
            dut.io.empty.expect(stack.size == 0)
            dut.io.almostEmpty.expect(stack.size <= emptyThreshold)
            dut.io.full.expect(stack.size == myParams.fifoDepth)
            dut.io.almostFull.expect(stack.size >= fullThreshold)
          }

          // Initialize the inputs
          dut.io.push.poke(false.B)
          dut.io.pop.poke(false.B)
          dut.io.almostFullLevel.poke(fullThreshold.U)
          dut.io.almostEmptyLevel.poke(emptyThreshold.U)
          dut.io.dataIn.poke(0.U)

          // Reset sequence
          dut.reset.poke(true.B)
          dut.clock.step()
          dut.reset.poke(false.B)

          /** Create randomized data of the correct width that can be used for
            * FIFO testing. Generation of such patterns for abitrary width is
            * challenging. This is done by constructing a BigInt out of hex
            * chars one nibble (or partial nibble) at a time then converting it
            * back into UInt for ChiselTest to apply it via a poke.
            */
          def testData(): UInt = {

          // format: off
          /** Generate hex choices that can be randomized for creating data
            * patterns. The function returns fewer choices when there is data is
            * less than a full nibble as in the example below with dataWidth =
            * 13
            *
            *                 12 11 10  9  8  7  6  5  4  3  2  1  0  
            * full nibbles       ----------- ----------- ----------- 
            * 1 extra bit     --
            *
            * In this case, bit 12 can only take a 0/1 value because it is not
            * large enough to hold any other hex value.
            */
            // format: on
            def getHexString(x: Int): String = {
              val hexStringRem0 = "0123456789abcdef" // no extra bits
              val hexStringRem1 = "01"               // one extra bit
              val hexStringRem2 = "0123"             // two extra bits
              val hexStringRem3 = "01234567"         // three extra bits
              return x match {
                case 0 => hexStringRem0
                case 1 => hexStringRem1
                case 2 => hexStringRem2
                case 3 => hexStringRem3
              }
            }

            /** Determine whether a partial nibble should be generated */
            def partialNibble(x: Int): Int = {
              if (x == 0) { return 0 }
              else { return 1 }
            }

            val numNibbles      = myParams.dataWidth / 4
            val numLeftOverBits = myParams.dataWidth % 4

          // format: off
          /** Generate two sets of random strings used for generation of the
            * randomized data. One used for full nibbles the other for partial
            * nibbles.
            * 
            * datawidth = 13
            * randFullNibble = "abc"
            * randPartialNibble = "1"
            */
            // format: on

            val randFullNibble    = getHexString(0)
            val randPartialNibble = getHexString(numLeftOverBits)

          // format: off
          /** Assemble the test data word by creating two Seqs. One consists of
            * full nibbles, the second is for the last partial nibble. The
            * partial nibble value is prepended to the full list of nibbles.
            * 
            * fullNibbleSeq = Seq[List[String]] = List(List(a, b, c))
            * partialNibbleSeq = Seq[List[String]] = List(List(1))
            * assembleSeq = Seq[List[String]] = List(List(1,a,b,c))
            */
          // format: on

            val fullNibbleSeq = Seq.fill(myParams.dataWidth / 4) {
              Random.shuffle(randFullNibble).head
            }
            val partialNibbleSeq = Seq.fill(partialNibble(numLeftOverBits)) {
              Random.shuffle(randPartialNibble).head
            }
            val assembledSeq = partialNibbleSeq ++ fullNibbleSeq

            // Create a hex BigInt and cast it to UInt
            val randData = BigInt(assembledSeq.mkString, 16).U

            return randData
          }

          // Create a buffer of randomized test data to apply in the test
          val testDataBuffer = Seq.fill(testDataSize)(testData())

          // Directed tests.  Fill up the FIFO then read it back
          stack.clear()
          testDataBuffer.foreach { data =>
            if (!isFull()) {
              stack.push(1)
              push(data)
              checkFlags()
            }
          }

          testDataBuffer.foreach { data =>
            if (!isEmpty()) {
              stack.pop()
              pop(data)
              checkFlags()
            }
          }

          /** Random tests. Randomly push/pop data to emulate backpressure in a
            * realsystem. Slightly more priority is given to push to insure FIFO
            * fills to full at some point for flag testing.
            */

          // Create a queue for tracking what data is pushed/popped
          val expectedData = scala.collection.mutable.Queue[UInt]()

          stack.clear()
          testDataBuffer.foreach { data =>
            fork {
              if (!isFull()) {
                if (math.random() < 0.75) {
                  expectedData.enqueue(data)
                  stack.push(1)
                  push(data)
                  checkFlags()
                }
              }
            }.fork {
              if (!isEmpty()) {
                if (math.random() < 0.55) {
                  val expected = expectedData.dequeue()
                  stack.pop()
                  pop(expected)
                  checkFlags()
                }
              }
            }.join()
          }
        }

      //  Report code coverage
      val coverage = cov.getAnnotationSeq
        .collectFirst { case a: TestCoverage => a.counts }
        .get
        .toMap
      val numTicks    = coverage("tick")
      val netCoverage = coverage.view.filterKeys(_ != "tick").toMap
      if (verbose) {
        println()
        println("------------------------------------------------")
        println("%\tCount\tCoverage Point")
        println("------------------------------------------------")
      }
      netCoverage.keys.foreach((coverPoint) => {
        val toggleCount = netCoverage(coverPoint)
        val togglePct   = toggleCount.toDouble / numTicks * 100
        if (verbose) {
          println(f"${togglePct}%1.2f\t${toggleCount}\t${coverPoint}")
        }
        if (togglePct == 0) {
          info(s"${coverPoint} is stuck at 0"); fail()
        }
        if (togglePct == 1) {
          info(s"${coverPoint} is stuck at 1"); fail()
        }
      })
      if (verbose) {
        println("------------------------------------------------")
        println()
      }
    }
  }

  // Execute the regression across a randomized range of configurations
  (1 to numTests).foreach { config =>
    main(s"DynamicFifo regression test - configuration $config")
  }

}
