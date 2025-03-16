// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)
package tech.rocksavage.chiselware.dynamicfifo

import chisel3._
import chisel3.util._
import chiseltest.formal.past
import tech.rocksavage.test.TestUtils.coverAll

/** A synchronous FIFO and FIFO controller with dynamic flags
  *
  * @constructor
  *   create a new FIFO or FIFO controller
  * @param p
  *   a set of parameters for the FIFO
  * @author
  *   Warren Savage
  * @todo
  *   emit license from FIRRTL
  * @see
  *   [[http://www.rocksavage.tech/]] for more information
  *
  * <img src="doc/images/user-guide/block-diagram.png" />
  */

class DynamicFifo(p: DynamicFifoParams) extends Module {

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

    val memWriteEnable  = WireDefault(false.B)
    val memWriteAddress = WireDefault(0.U(log2Ceil(p.fifoDepth).W))

    val fifoMemory = Module(new DynamicFifoMem(p))

    val head  = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))
    val tail  = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))
    val count = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))

    /** When push is asserted && the fifo is not full increment count
      */
    val pushValid = io.push && (count =/= p.fifoDepth.U)
    when(pushValid) {
        head            := increment(head, p.fifoDepth.U - 1.U)
        memWriteEnable  := true.B
        memWriteAddress := head
    }

    /** When pop is asserted && the fifo is not empty decrement count
      */
    val popValid = io.pop && (count =/= 0.U)
    when(popValid) {
        tail := increment(tail, p.fifoDepth.U - 1.U)
    }

    when(pushValid && popValid) {
        count := count
    }.elsewhen(pushValid && !popValid) {
        count := count + 1.U
    }.elsewhen(!pushValid && popValid) {
        count := count - 1.U
    }

    io.empty       := count === 0.U
    io.full        := count === p.fifoDepth.U
    io.almostEmpty := count <= io.almostEmptyLevel
    io.almostFull  := count >= io.almostFullLevel

    fifoMemory.io.readEnable   := true.B
    fifoMemory.io.writeEnable  := memWriteEnable
    fifoMemory.io.readAddress  := tail
    fifoMemory.io.writeAddress := memWriteAddress
    fifoMemory.io.writeData    := io.dataIn

    io.dataOut := fifoMemory.io.readData

    def increment(value: UInt, max: UInt): UInt = {
        Mux(value === max, 0.U, value + 1.U)
    }

    // Collect code coverage points
    if (p.coverage) {
        coverAll(io)
    }

    if (p.formal) {

        // ### Behavioral Model ###
        // 1. Assert Count exactly matches the number of elements in the FIFO

        val numElementsReg = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))
        val numElements    = WireDefault(0.U(log2Ceil(p.fifoDepth + 1).W))

        when(pushValid & !popValid) {
            numElements := numElementsReg + 1.U
        }.elsewhen(!pushValid & popValid) {
            numElements := numElementsReg - 1.U
        }.elsewhen(pushValid & popValid) {
            numElements := numElementsReg
        }.otherwise {
            numElements := numElementsReg
        }
        numElementsReg := numElements
        assert(numElementsReg === count)

        // 2. Assert that the number of elements in the FIFO is within the bounds of the FIFO depth
        assert(numElementsReg >= 0.U)
        assert(numElementsReg <= p.fifoDepth.U)

        // 3. Assert that the elements exit in the same order they entered
        val fifoModel = RegInit(
          VecInit(Seq.fill(p.fifoDepth)(0.U(p.dataWidth.W)))
        )

        // when popped, shift all elements down by one
        for (i <- 0 until p.fifoDepth - 1) {
            when(popValid) {
                fifoModel(i) := fifoModel(i + 1)
            }
        }
        // when pushed, add the new element to the correct index
        when(pushValid) {
            fifoModel(numElements - 1.U) := io.dataIn
        }

        // when popped, the dataOut should match the head of the fifo
        when(popValid) {
            assert(fifoModel(0) === io.dataOut)
        }

        // ### Flag Assertions ###
        // 1. if count == 0, then the fifo is empty
        when(numElementsReg === 0.U) {
            assert(io.empty)
        }

        // 2. if count == p.fifoDepth, then the fifo is full
        when(numElementsReg === p.fifoDepth.U) {
            assert(io.full)
        }

        // 3. if count <= io.almostEmptyLevel, then the fifo is almost empty
        when(numElementsReg <= io.almostEmptyLevel) {
            assert(io.almostEmpty)
        }

        // 4. if count >= io.almostFullLevel, then the fifo is almost full
        when(numElementsReg >= io.almostFullLevel) {
            assert(io.almostFull)
        }

        // ### Push Assertions ###
        // 1. pushValid and count < p.fifoDepth
        when(io.push && numElementsReg < p.fifoDepth.U) {
            assert(pushValid)
        }

        // 2. pushValid and count == p.fifoDepth
        when(io.push && numElementsReg === p.fifoDepth.U) {
            assert(!pushValid)
        }

        // 3. head = head + 1
        when(past(pushValid)) {
            // either head is incremented by 1 or it wraps around to 0 if it reaches p.fifoDepth
            when(past(head) === p.fifoDepth.U - 1.U) {
                assert(head === 0.U)
            }.otherwise {
                assert(past(head) + 1.U === head)
            }
        }

        // 4. memWriteEnable = true
        when(pushValid) {
            assert(memWriteEnable)
        }

        // ### Pop Assertions ###
        // 1. popValid and count > 0
        when(io.pop && numElementsReg > 0.U) {
            assert(popValid)
        }

        // 2. popValid and count == 0
        when(io.pop && numElementsReg === 0.U) {
            assert(!popValid)
        }

        // 3. tail = tail + 1
        when(past(popValid)) {
            // either tail is incremented by 1 or it wraps around to 0 if it reaches p.fifoDepth
            when(past(tail) === p.fifoDepth.U - 1.U) {
                assert(tail === 0.U)
            }.otherwise {
                assert(past(tail) + 1.U === tail)
            }
        }

        // 4. memReadEnable = true
        when(popValid) {
            assert(fifoMemory.io.readEnable)
        }
    }
}
