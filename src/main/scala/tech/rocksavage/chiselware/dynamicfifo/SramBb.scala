//// (c) 2024 Rocksavage Technology, Inc.
//// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)
//
//package tech.rocksavage.chiselware.dynamicfifo
//
//import chisel3._
//import chisel3.util._
//
///** Blackbox to hold Verilog simulation model */
//class SramBb(p: BaseParams)
//    extends BlackBox(
//      Map("ADDR_WIDTH" -> log2Ceil(p.fifoDepth), "DATA_WIDTH" -> p.dataWidth)
//    )
//    with HasBlackBoxResource {
//  override val desiredName = "simple_dual_one_clock"
//  val io = IO(new Bundle {
//    val clk           = Input(Clock())
//    val read_enable   = Input(Bool())
//    val write_enable  = Input(Bool())
//    val read_address  = Input(UInt(log2Ceil(p.fifoDepth).W))
//    val write_address = Input(UInt(log2Ceil(p.fifoDepth).W))
//    val write_data    = Input(UInt(p.dataWidth.W))
//    val read_data     = Output(UInt(p.dataWidth.W))
//
//    for (file <- p.bbFiles) addResource(s"vsrc/${file}")
//
//  })
//}
