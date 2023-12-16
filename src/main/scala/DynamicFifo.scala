// This confidential and proprietary software may be used only as authorized by
// Rocksavage Technology, Inc. In the event of publication the following
// notice is applicable:
//
// (C) COPYRIGHT 2023 ROCKSAVAGE TECHNOLOGY, INC.
// ALL RIGHTS RESERVED
package tech.rocksavage.chiselware.DynamicFifo

import chisel3._
import chisel3.util._
import java.io.{File, PrintWriter}
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
  *   [[http://www.rocksavage.tech/]] for more information
  *
  * <img src="doc/images/user-guide/block-diagram.png" />
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
    val ramWriteEnable   = Output(Bool())
    val ramWriteAddress  = Output(UInt(log2Ceil(p.fifoDepth).W))
    val ramDataIn        = Output(UInt(p.dataWidth.W))
    val ramReadEnable    = Output(Bool())
    val ramReadAddress   = Output(UInt(log2Ceil(p.fifoDepth).W))
    val ramDataOut       = Input(UInt(p.dataWidth.W))
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

  io.dataOut         := Mux(p.externalRam.B, io.ramDataOut, fifoMemory(tail))
  io.ramDataIn       := io.dataIn
  io.ramReadAddress  := tail
  io.ramWriteAddress := head
  io.ramReadEnable   := io.pop
  io.ramWriteEnable  := io.push

  // Collect code coverage points
  if (p.coverage) {
    // count clock ticks to allow for coverage computation
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

  //format: off
  /** Generate a basic SDC file for use with post-synthesis static timing
    * analysis
    *
    * The illustration below shows the basic elements captured in an SDC file.
    * 
    * The follow variables are as follows:
    *   period: inverse of the expected operating frequency 
    *   dutyCycle: ratio of time when the clock is high and low (typ. 50%) 
    *   inputDelay: time when the data is valid from the last rising clock edge 
    *   outputDelay: time when the data is valid to the next rising clock edge
    *
    *   +-------+ 
    * a-|       |
    *   |  and  |--> c 
    * b-|       | 
    *   +-------+
    *
    *            |<--------->| Period 
    *            |<--->|<--->| Duty Cycle 
    *             _____       _____       _____ 
    * clock _____|     |_____|     |_____|     |_____
    *
    *            |<->|       |<->| Input Delay 
    *                 _______________________________
    *     a _________|
    *                             ___________________ 
    *     b _____________________| 
    * 
    *                                |<->| Output Delay
    *                                ________________ 
    * c ____________________________|
    *
    */
  //format:on 

  // Basic constraints
  val period      = 5.000 // ns
  val dutyCycle   = 0.50
  val inputDelayPct = 0.2
  val outputDelayPct = 0.2

  // Calculated constraints, override as needed below
  val inputDelay  = period * inputDelayPct
  val outputDelay = period * outputDelayPct
  val fallingEdge = period * dutyCycle
  val sdc         = new File("./syn/DynamicFifo.sdc")
  val sdcFile     = new PrintWriter(sdc)

  val sdcFileData = s"""
    create_clock -period $period -waveform {0 $fallingEdge} -name clock
    set_input_delay -clock clock $inputDelay {reset}
    set_input_delay -clock clock $inputDelay {io_pop}
    set_input_delay -clock clock $inputDelay {io_push}
    set_input_delay -clock clock $inputDelay {io_dataIn}
    set_input_delay -clock clock $inputDelay {io_almostFullLevel}
    set_input_delay -clock clock $inputDelay {io_almostEmptyLevel}
    set_input_delay -clock clock $inputDelay {io_ramDataOut}
    set_output_delay -clock clock $outputDelay {io_dataOut}
    set_output_delay -clock clock $outputDelay {io_empty}
    set_output_delay -clock clock $outputDelay {io_full}
    set_output_delay -clock clock $outputDelay {io_almostEmpty}
    set_output_delay -clock clock $outputDelay {io_almostFull}
    set_output_delay -clock clock $outputDelay {io_ramDataIn}
    set_output_delay -clock clock $outputDelay {io_ramWriteEnable}
    set_output_delay -clock clock $outputDelay {io_ramReadEnable}
  """.stripMargin

  sdcFile.write(s"${sdcFileData}")
  sdcFile.close()

}

/** Generate Verilog */
object Main extends App {
  val myParams = BaseParams(
    externalRam = true,
    dataWidth = 128,
    fifoDepth = 32
  )
  ChiselStage.emitSystemVerilog(
    new DynamicFifo(myParams),
    firtoolOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      "--verilog",
      "--split-verilog",
      "-o=generated"
    )
  )
}
