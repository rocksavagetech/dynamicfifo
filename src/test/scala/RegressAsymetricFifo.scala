// This confidential and proprietary software may be used only as authorized by
// Rocksavage Technology, Inc. In the event of publication the following
// notice is applicable:
//
// (C) COPYRIGHT 2021 ROCKSAVAGE TECHNOLOGY, INC.
// ALL RIGHTS RESERVED
//
// The entire notice must be reproduced on all authorized copies.
//
// File         : RegressPrng16
// Author       : Warren Savage
// Abstract     : Regression tester for Prng16
//
// MODIFICATION HISTORY
// ============================================================================
// Date         Version   By                Description
// ----------------------------------------------------------------------------
// 2021-07-25   1.0       Warren Savage     Initial version
// 2022-01-22   1.1       Warren Savage     Port to Chisel 3.5 + ChiselTest
//
// ============================================================================

package chiselWare

import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.coverage._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import firrtl.options.TargetDirAnnotation

class RegressAsymmeticFifo
    extends AnyFlatSpec
    with Matchers
    with ChiselScalatestTester {

  // Available backends, default is Treadle, uncomment additional backend options
  val backendAnnotations =
    Seq(
      // WriteVcdAnnotation,
      // WriteFstAnnotation,
      VerilatorBackendAnnotation,
      // IcarusBackendAnnotation,
      // VcsBackendAnnotation,
      TargetDirAnnotation("generated")
    )
  /*
  behavior of "Prng16 regression test"
  it should "verify that 64K-1 unique patterns are generated" in {
    val r = test(new Prng16).withAnnotations(backendAnnotations) { dut =>
      // let the clock run
      dut.clock.setTimeout(0)

      // count clocks so as to be able to calculate code coverage

      // create a non-zero random seed
      val rand     = scala.util.Random
      val prngSeed = rand.nextInt(65536)
      while (prngSeed == 0) {
        val prngSeed = rand.nextInt(65536)
      }

      dut.io.seed.poke(prngSeed.U)
      info("Seed is: 0x" + dut.io.seed.peek().litValue.toString(16))
      info("Testing that seed is loaded at Reset")
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.io.out.expect(prngSeed.U)

      info("Testing that PRNG does not advance with enable is held low")
      dut.io.enable.poke(false.B)
      dut.clock.step(2)
      dut.io.out.expect(prngSeed.U)

      info("Testing that PRNG generates 64K-1 unique values")
      val scorecard = Array.fill(65536)(false)
      var scoreFail = false
      dut.io.enable.poke(true.B)

      // Generate all the patterns and write into the scorecard that the pattern was seen
      for (i <- 0 to 65535) {
        val index = dut.io.out.peek().litValue.toInt
        scorecard(index) = true
        dut.clock.step()
      }

      // Check the scorecard that it is completely full, except for index=0
      for (j <- 1 to 65535) {
        if (scorecard(j) == false) {
          info("Pattern " + j.toHexString + " was not generated")
          scoreFail = true
        }
      }

      info("Testing that PRNG doesn't generate 0x0000")
      if (scorecard(0) == true) {
        info("Pattern of 0 was incorrectly generated")
        scoreFail = true
      }

      // Throw an error if 64K-1 unique patterns were not generated
      scoreFail should equal(false)
    }

  //  Report code coverage (uncomment lines for full report)
  val coverage = r.getAnnotationSeq
    .collectFirst { case a: TestCoverage => a.counts }
    .get
    .toMap
  val numTicks    = coverage("tick")
  val netCoverage = coverage.view.filterKeys(_ != "tick").toMap
//    println("%\tCount\tCoverage Point")
//    println("------------------------------------------------")
  netCoverage.keys.foreach((coverPoint) => {
    val toggleCount = netCoverage(coverPoint)
    val togglePct   = toggleCount.toDouble / numTicks * 100
//      println(f"${togglePct}%1.2f\t${toggleCount}\t${coverPoint}")
    if (togglePct == 0) info(s"${coverPoint} is stuck at 0")
    if (togglePct == 1) info(s"${coverPoint} is stuck at 1")
  })

   */
}
