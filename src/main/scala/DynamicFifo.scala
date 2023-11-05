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

package DynamicFifo

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

/** A synchronous FIFO and FIFO controller with dynamic flags
  *
  * @constructor
  *   create a new FIFO or FIFO controller
  * @param externalRam
  *   creates just the FIFO controller (uses ext. SRAM)
  * @param dataWidth
  *   defines the data width of the FIFO
  * @param fifoDepth
  *   defines the depth of the FIFO (must be a power of 2)
  * @author
  *   Warren Savage
  * @todo
  *   emit license from FIRRTL
  * @see
  *   [[http://www.rocksavage.tech/Olympus.html]] for more information
  *
  * <img src="" />
  */
class DynamicFifo(p: BaseParams) extends Module {

  val io = IO(new Bundle {
    val push             = Input(Bool())
    val pop              = Input(Bool())
    val dataIn           = Input(UInt(p.dataWidth.W))
    val dataOut          = Output(UInt(p.dataWidth.W))
    val empty            = Output(Bool())
    val full             = Output(Bool())
    val almostEmpty      = Output(Bool())
    val almostFull       = Output(Bool())
    val almostEmptyLevel = Input(UInt(log2Ceil(p.fifoDepth).W))
    val almostFullLevel  = Input(UInt(log2Ceil(p.fifoDepth).W))
    val ramWriteEnable  = Output(Bool())
    val ramWriteAddress = Output(UInt(log2Ceil(p.fifoDepth).W))
    val ramDataIn       = Output(UInt(p.dataWidth.W))
    val ramReadEnable   = Output(Bool())
    val ramReadAddress  = Output(UInt(log2Ceil(p.fifoDepth).W))
    val ramDataOut      = Input(UInt(p.dataWidth.W))
  })

  val fifoMemory = Reg(Vec(p.fifoDepth, UInt(p.dataWidth.W)))

  val tail  = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))
  val head  = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))
  val count = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))

  when(io.push === 1.U) { count := count + 1.U }
  when(io.pop === 1.U) { count := count - 1.U }
  when((io.pop === 1.U) && io.push === 1.U) { count := count }

  io.empty       := count === 0.U
  io.full        := count === p.fifoDepth.U
  io.almostEmpty := count <= io.almostEmptyLevel
  io.almostFull  := count >= io.almostFullLevel

  when(io.pop && !io.empty) {
    tail := (tail + 1.U)
  }

  when(io.push && !io.full) {
    fifoMemory(head) := io.dataIn
    head             := (head + 1.U)
  }

  io.dataOut := Mux(p.externalRam.B, io.ramDataOut, fifoMemory(tail))
  io.ramDataIn       := io.dataIn
  io.ramReadAddress  := tail
  io.ramWriteAddress := head
  io.ramReadEnable   := io.pop
  io.ramWriteEnable  := io.push

  // Collect code coverage points
  if (p.coverage) {
    val tick = true.B
    for (bit <- 0 to p.dataWidth - 1) {
      cover(io.dataOut(bit)).suggestName(s"io_dataOut_$bit")
      cover(io.dataIn(bit)).suggestName(s"io_dataIn_$bit")
    }

    /* Ignore intentionally static inputs
    for (bit <- 0 to log2Ceil(fifoDepth) - 1) {
      cover(io.almostEmptyLevel(bit)).suggestName(s"io_almostEmptyLevel_$bit")
      cover(io.almostFullLevel(bit)).suggestName(s"io_almostFullLevel_$bit")
    }
    */

    cover(tick).suggestName("tick")
    cover(io.pop).suggestName("io__pop")
    cover(io.push).suggestName("io__push")
    cover(io.almostEmpty).suggestName("io__empty")
    cover(io.almostFull).suggestName("io__full")
    cover(io.almostEmpty).suggestName("io__almostEmpty")
    cover(io.almostFull).suggestName("io__almostFull")

    if (p.externalRam) {
      cover(io.ramWriteEnable).suggestName("io__ramWriteEnable")
      cover(io.ramReadEnable).suggestName("io__ramReadEnable")
      for (bit <- 0 to p.dataWidth - 1) {
        cover(io.ramDataIn(bit)).suggestName(s"io_ramDataIn_$bit")
        cover(io.ramDataOut(bit)).suggestName(s"io_ramDataOut_$bit")
      }
      for (bit <- 0 to log2Ceil(p.fifoDepth) - 1) {
        cover(io.ramReadAddress(bit)).suggestName(s"io_ramReadAddress_$bit")
        cover(io.ramWriteAddress(bit)).suggestName(s"io_ramWriteAddress_$bit")
      }
    }
  }

}

/** Generate Verilog */
object DynamicFifo extends App {
  val myParams = BaseParams(
    externalRam = true,
    dataWidth = 128,
    fifoDepth = 32
  )
    ChiselStage.emitSystemVerilog(
      new DynamicFifo(myParams),
      firtoolOpts = Array(
        "--disable-all-randomization",
        "--strip-debug-info",
        "--split-verilog",
        "-o=generated"
      )
    )
}
