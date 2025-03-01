// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package tech.rocksavage.chiselware.dynamicfifo

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
  * @param formal
  *   specifies whether to run formal verification
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
    coverage: Boolean = false,
    formal: Boolean = false
) {

//  require(dataWidth >= 4, "Width must be greater than or equal 4")
  require(fifoDepth >= 2, "Depth must be greater than or equal 2")
}
