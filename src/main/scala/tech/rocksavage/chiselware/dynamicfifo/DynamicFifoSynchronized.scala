// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package tech.rocksavage.chiselware.dynamicfifo

import chisel3._
import chisel3.util._

class DynamicFifoSynchronized(p: DynamicFifoParams) extends Module {
  val io = IO(new Bundle {
    val push             = Input(Bool())
    val pop              = Input(Bool())
    val flush            = Input(Bool())
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



  dut.io.push             := RegNext(io.push)
  dut.io.pop              := RegNext(io.pop)
  dut.io.flush            := RegNext(io.flush)
  dut.io.dataIn           := RegNext(io.dataIn)
  dut.io.almostEmptyLevel := RegNext(io.almostEmptyLevel)
  dut.io.almostFullLevel  := RegNext(io.almostFullLevel)
  io.dataOut              := dut.io.dataOut
  io.empty                := dut.io.empty
  io.full                 := dut.io.full
  io.almostEmpty          := dut.io.almostEmpty
  io.almostFull           := dut.io.almostFull

}
