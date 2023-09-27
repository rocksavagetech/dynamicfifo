// This confidential and proprietary software may be used only as authorized by
// Rocksavage Technology, Inc. In the event of publication the following
// notice is applicable:
//
// (C) COPYRIGHT 2023 ROCKSAVAGE TECHNOLOGY, INC.
// ALL RIGHTS RESERVED
//
// The entire notice must be reproduced on all authorized copies.
//
// File         : DynamicFifo.scala
// Author       : Warren Savage
// Abstract     : FIFO and FIFO controller with dynamic thresholds
//
// MODIFICATION HISTORY
// ============================================================================
// Date         Version   By                Description
// ----------------------------------------------------------------------------
// 2023-09-09   x.x       Warren Savage     Initial version
//
// ============================================================================

package chiselWare

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

class DynamicFifo(
    externalRam: Boolean,
    dataWidth: Int,
    fifoDepth: Int
) extends Module {
  val io = IO(new Bundle {
    val push             = Input(Bool())
    val pop              = Input(Bool())
    val dataIn           = Input(UInt(dataWidth.W))
    val dataOut          = Output(UInt(dataWidth.W))
    val empty            = Output(Bool())
    val full             = Output(Bool())
    val almostEmpty      = Output(Bool())
    val almostFull       = Output(Bool())
    val almostEmptyLevel = Input(UInt(log2Ceil(fifoDepth).W))
    val almostFullLevel  = Input(UInt(log2Ceil(fifoDepth).W))
    // Optional if External RAM is chosen
    val ramWriteEnable   = Output(Bool())
    val ramWriteAddress  = Output(UInt(log2Ceil(fifoDepth).W))
    val ramDataIn        = Output(UInt(dataWidth.W))
    val ramReadEnable    = Output(Bool())
    val ramReadAddress   = Output(UInt(log2Ceil(fifoDepth).W))
    val ramDataOut       = Input(UInt(dataWidth.W))
  })

  // Stub code to allow compilation
  io.dataOut := 0.U
  io.empty := 0.B
  io.full := 0.B
  io.almostEmpty := 0.B
  io.almostFull := 0.B
  io.ramWriteEnable := 0.B
  io.ramWriteAddress := 0.U
  io.ramDataIn := 0.U
  io.ramReadEnable := 0.B
  io.ramReadAddress := 0.U

  // Collect code coverage points
  val tick = true.B
  for (i <- 0 to dataWidth-1) {
    cover(io.dataOut(i)).suggestName(s"io.dataOut_{i}")
    cover(io.dataIn(i)).suggestName(s"io.dataIn_{i}")
  }
  for (i <- 0 to log2Ceil(fifoDepth)-1) {
    cover(io.almostEmptyLevel(i)).suggestName("io.almostEmptyLeveli_{i}")
    cover(io.almostFullLevel(i)).suggestName("io.almostFullLevel_{i}")
  }
  cover(tick).suggestName("tick")
  cover(io.pop).suggestName("io.pop")
  cover(io.push).suggestName("io.push")
  cover(io.almostEmpty).suggestName("io.empty")
  cover(io.almostFull).suggestName("io.full")
  cover(io.almostEmpty).suggestName("io.almostEmpty")
  cover(io.almostFull).suggestName("io.almostFull")

  // Only relevant when externamRAM is set to true
  cover(io.ramWriteEnable).suggestName("io.ramWriteEnable")
  cover(io.ramReadEnable).suggestName("io.ramReadEnable")
  for (i <- 0 to dataWidth-1) {
    cover(io.ramDataIn(i)).suggestName("io.ramDataIni_{i}")
    cover(io.ramDataOut(i)).suggestName("io.ramDataOut_{i}")
  }
  for (i <- 0 to log2Ceil(fifoDepth)-1) {
    cover(io.ramReadAddress(i)).suggestName("io.ramReadAddress_{i}")
    cover(io.ramWriteAddress(i)).suggestName("io.ramWriteAddress_{i}")
  }

}


// Application to generate verilog RTL
object DynamicFifo extends App {
  (new chisel3.stage.ChiselStage).execute(
    Array("-X", "sverilog", "--target-dir", "generated"),
    Seq(ChiselGeneratorAnnotation(() => 
      new DynamicFifo(
        externalRam = true,
        dataWidth = 16,
        fifoDepth = 5
      )))
  )
}