// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package tech.rocksavage.chiselware.dynamicfifo

import chisel3._
import chisel3.util._

class DynamicFifoMem(p: DynamicFifoParams) extends Module {

  val io = IO(new Bundle {
    val readEnable   = Input(Bool())
    val writeEnable  = Input(Bool())
    val readAddress  = Input(UInt(log2Ceil(p.fifoDepth).W))
    val writeAddress = Input(UInt(log2Ceil(p.fifoDepth).W))
    val writeData    = Input(UInt(p.dataWidth.W))
    val readData     = Output(UInt(p.dataWidth.W))
  })

  // simple dual port memory won't work for this for timing requirements
  // i.e. a push on one cycle and a pop on the next cycle won't work properly,
  // a simple dual port memory will need one extra cycle to read the data
  // val mem = SRAM(p.fifoDepth, UInt(p.dataWidth.W), numReadPorts = 1, numWritePorts = 1, numReadwritePorts = 0)

  // vec of regs
  val mem      = RegInit(VecInit(Seq.fill(p.fifoDepth)(0.U(p.dataWidth.W))))
  val readData = WireDefault(0.U(p.dataWidth.W))

  when(io.writeEnable) {
    mem(io.writeAddress) := io.writeData
  }
  when(io.readEnable) {
    readData := mem(io.readAddress)
  }

  io.readData := readData
}
