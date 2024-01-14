// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under MIT license (see LICENSE.MD for details)

package tech.rocksavage.chiselware.DynamicFifo

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

// Generate some Verilog test cases for synthesis
object GenVerilog extends App {
  // Create a map with configurations to generate
  val config = Map(
    // name  -> externalRam, dataWidth, fifoDepth
    "small_false_8_8"     -> Vector(false, 8, 8),
    "medium_false_32_64"  -> Vector(false, 32, 64),
    "large_false_64_256"  -> Vector(false, 64, 256),
    "small_true_64_256"   -> Vector(true, 64, 256),
    "medium_true_128_128" -> Vector(true, 128, 1028),
    "large_true_256_2048" -> Vector(true, 256, 2048)
  )

  config.foreach { case (testName, paramVec) =>
    val thisExternalRam = paramVec(0).asInstanceOf[Boolean]
    val thisDataWidth   = paramVec(1).asInstanceOf[Int]
    val thisFifoDepth   = paramVec(2).asInstanceOf[Int]
    val myParams = BaseParams(
      externalRam = thisExternalRam,
      dataWidth = thisDataWidth,
      fifoDepth = thisFifoDepth
    )

    println(
      s"Generating Verilog config: $testName\t" +
        s"externalRam = $thisExternalRam\t" +
        s"dataWidth = $thisDataWidth\t " +
        s"fifoDepth = $thisFifoDepth"
    )

    // Generate basic Verilog (suppress SV features with lowering, etc)
    ChiselStage.emitSystemVerilog(
      new DynamicFifo(myParams),
      firtoolOpts = Array(
        "--lowering-options=disallowLocalVariables,disallowPackedArrays",
        "--disable-all-randomization",
        "--strip-debug-info",
        "--split-verilog",
        s"-o=generated/synTestCases/$testName"
      )
    )
  }
}
