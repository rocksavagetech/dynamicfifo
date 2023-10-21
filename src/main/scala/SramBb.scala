// This confidential and proprietary software may be used only as authorized by
// Rocksavage Technology, Inc. In the event of publication the following
// notice is applicable:
//
// (C) COPYRIGHT 2023 ROCKSAVAGE TECHNOLOGY, INC.
// ALL RIGHTS RESERVED
//
// The entire notice must be reproduced on all authorized copies.
//
// File         : SramBb.scala
// Author       : Warren Savage
// Abstract     : Blackbox for synchronous dual-port SRAM
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
//import chisel3.experimental._

class SramBb (p: BaseParams) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val read_enable = Input(Bool())
    val write_enable = Input(Bool())
    val read_address = Input(UInt(log2Ceil(p.fifoDepth).W))
    val write_address = Input(UInt(log2Ceil(p.fifoDepth).W))
    val data_in = Input(UInt(p.dataWidth.W))
    val data_out = Output(UInt(p.dataWidth.W))

    for (file <- p.bbFiles) addResource(s"vsrc/${file}")

  })
}