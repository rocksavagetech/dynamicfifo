// This confidential and proprietary software may be used only as authorized by
// Rocksavage Technology, Inc. In the event of publication the following
// notice is applicable:
//
// (C) COPYRIGHT 2021 ROCKSAVAGE TECHNOLOGY, INC.
// ALL RIGHTS RESERVED
package tech.rocksavage.chiselware.DynamicFifo

import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.coverage._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Assertions._
import firrtl2.options.TargetDirAnnotation
import scala.util.Random
import scala.math.pow
import scala.collection.mutable.Stack
import scala.collection.immutable.ListMap
import java.io.{File, FileWriter, PrintWriter, BufferedWriter}
import tech.rocksavage.chiselware.util.TestUtils.{randData, checkCoverage}

/** Highly randomized test suite driven by configuration parameters. Includes
  * code coverage for all top-level ports.
  */
class DynamicFifoTest
    extends AnyFlatSpec
    with Matchers
    with ChiselScalatestTester {

  val numTests = 50
  val verbose  = false

  /** main test function executes one test for one configuration */
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
    val validDataWidths = Seq(4, 5, 6, 7, 8, 16, 32, 64, 128)
    val validFifoDepths = Seq(4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048)
    val dataWidth      = validDataWidths(Random.nextInt(validDataWidths.length))
    val fifoDepth      = validFifoDepths(Random.nextInt(validFifoDepths.length))
    val useExternalRam = (fifoDepth * dataWidth) > 1024
    val testDataSize   = fifoDepth * Random.between(10, 20)
    val fullThreshold  = (fifoDepth * Random.between(75, 95) / 100).toInt
    val emptyThreshold = (fifoDepth * Random.between(3, 25) / 100).toInt

    val myParams = BaseParams(
      dataWidth,
      fifoDepth,
      useExternalRam,
      bbFiles = List("dual_port_sync_sram.v"),
      coverage = true
    )

    it should "test this configuration" in {
      info(s"Test Data Size = $testDataSize")
      info(s"Data Width = $dataWidth")
      info(s"FIFO Depth = $fifoDepth")
      info(s"Almost Full Threshold = $fullThreshold")
      info(s"Almost Empty Threshold = $emptyThreshold")
      info("--------------------------------")
      val cov = test(new DynamicFifoTb(myParams))
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

          // Use Scala Stack to mirror the FIFO operations to check flags
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

          // Create a buffer of randomized test data to apply in the test
          val testDataBuffer =
            Seq.fill(testDataSize)(randData(myParams.dataWidth))

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

          // Create a queue for tracking what data is pushed/popped
          val expectedData = scala.collection.mutable.Queue[UInt]()
          stack.clear()

          /** Random tests. Randomly push/pop data to emulate backpressure in a
            * realsystem. Slightly more priority is given to push to insure FIFO
            * fills to full at some point for flag testing.
            */
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

      // Check that all ports have toggled and print report
      if (myParams.coverage) {
        val coverage = cov.getAnnotationSeq
          .collectFirst { case a: TestCoverage => a.counts }
          .get
          .toMap

        val testConfig =
          myParams.externalRam.toString + "_" +
            myParams.dataWidth.toString + "_" +
            myParams.fifoDepth.toString

        val verCoverageDir = new File("generated/verilogCoverage")
        verCoverageDir.mkdir()
        val coverageFile =
          verCoverageDir.toString + "/" + testName + "_" + testConfig + ".cov"

        val stuckAtFault = checkCoverage(coverage, coverageFile)
        if (stuckAtFault)
          fail(s"At least one IO port did not toggle -- see $coverageFile")
        info(s"Verilog Coverage report written to $coverageFile")
      }
    }
  }

  // Create a directory for storing coverage reportsG
  val scalaCoverageDir = new File("generated/scalaCoverage")
  scalaCoverageDir.mkdir()

  // Execute the regression across a randomized range of configurations
  (1 to numTests).foreach { config =>
    main(s"DynamicFifo_test_config_$config")
  }

  // Test that illegal combinations are caught
  it should "throw an assertion when dataWidth parameter is less than 4" in {
    val myParams = assertThrows[IllegalArgumentException] {
      BaseParams(
        dataWidth = 3,
        bbFiles = List("dual_port_sync_sram.v")
      )
    }
  }

  it should "throw an assertion when fifoDepth parameter is not a power of 2" in {
    val myParams = assertThrows[IllegalArgumentException] {
      BaseParams(
        fifoDepth = 7,
        bbFiles = List("dual_port_sync_sram.v")
      )
    }
  }

}
