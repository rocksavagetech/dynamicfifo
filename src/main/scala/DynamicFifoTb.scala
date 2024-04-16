// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package tech.rocksavage.chiselware.DynamicFifo

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class DynamicFifoTb(p: BaseParams) extends Module {
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
  })

  val dut = Module(new DynamicFifo(p))
  dut.io.push             := io.push
  dut.io.pop              := io.pop
  dut.io.dataIn           := io.dataIn
  dut.io.almostEmptyLevel := io.almostEmptyLevel
  dut.io.almostFullLevel  := io.almostFullLevel
  io.dataOut              := dut.io.dataOut
  io.empty                := dut.io.empty
  io.full                 := dut.io.full
  io.almostEmpty          := dut.io.almostEmpty
  io.almostFull           := dut.io.almostFull

  val ram = Module(new SramBb(p))
  ram.io.clk           := clock
  ram.io.write_enable  := dut.io.ramWriteEnable
  ram.io.write_address := dut.io.ramWriteAddress
  ram.io.read_enable   := dut.io.ramReadEnable
  ram.io.read_address  := dut.io.ramReadAddress
  ram.io.write_data    := dut.io.ramDataIn
  dut.io.ramDataOut    := ram.io.read_data

}
