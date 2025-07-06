// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package tech.rocksavage.chiselware.dynamicfifo

import chisel3._
import chisel3.util._

/** Default parameter settings for Dynamic FIFO
  *
  * @constructor
  *   default parameter settings
  * @param dataWidth
  *
  * specifies the width of the FIFO data
  * @param fifoDepth
  *   specifices the depth of the FIFO
  * @param externalRam
  *   specifies whether to use an external SRAM or generate internal FFs
  * @param coverage
  *   specifies whether to calculate port coverage during simulation
  * @author
  *   Warren Savage
  * @version 1.0
  *
  * @see
  *   [[http://www.rocksavage.tech]] for more information
  */
case class DynamicFifoParams(
    dataWidth: Int = 8,
    fifoDepth: Int = 8,
    externalRam: Boolean = false,
    coverage: Boolean = false,
    bbFiles: List[String] = List("your_sram.v")
) {

  require(dataWidth >= 4, "Width must be greater than or equal 4")
  require((fifoDepth % 2) == 0, "Depth must be a power of 2")
}
