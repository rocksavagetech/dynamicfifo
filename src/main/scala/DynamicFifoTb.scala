package chiselWare.DynamicFifo

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

  val myFifo = Module(new DynamicFifo(p))
  myFifo.io.push             := io.push
  myFifo.io.pop              := io.pop
  myFifo.io.dataIn           := io.dataIn
  myFifo.io.almostEmptyLevel := io.almostEmptyLevel
  myFifo.io.almostFullLevel  := io.almostFullLevel
  io.dataOut                 := myFifo.io.dataOut
  io.empty                   := myFifo.io.empty
  io.full                    := myFifo.io.full
  io.almostEmpty             := myFifo.io.almostEmpty
  io.almostFull              := myFifo.io.almostFull

  val myRam = Module(new SramBb(p))
  myRam.io.clk         := clock
  myRam.io.write_enable  := myFifo.io.ramWriteEnable
  myRam.io.write_address := myFifo.io.ramWriteAddress
  myRam.io.read_enable   := myFifo.io.ramReadEnable
  myRam.io.read_address  := myFifo.io.ramReadAddress
  myRam.io.write_data := myFifo.io.ramDataIn
  myFifo.io.ramDataOut   := myRam.io.read_data

}

object DynamicFifoTb extends App {
  val myParams = BaseParams(
    externalRam = true,
    dataWidth = 128,
    fifoDepth = 32
  )
  println(
    ChiselStage.emitSystemVerilog(
      new DynamicFifoTb(myParams),
      firtoolOpts = Array(
        "--disable-all-randomization",
        "--strip-debug-info",
        "--split-verilog",
        "-o=generated"
      )
    )
  )
}