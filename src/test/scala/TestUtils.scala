// (c) 2024 Rocksavage Technology, Inc.
// This code is licensed under MIT license (see LICENSE.MD for details)

package tech.rocksavage.chiselware.DynamicFifo

import chisel3._
import scala.util.Random
import scala.collection.immutable.ListMap
import java.time._
import java.time.format._
import java.io.{File, PrintWriter}

/** Collection of chiselWare utilities */
object TestUtils {

  /** Checks coverage and writes results to a file
    *
    * All chiselWare-compliant cores must have tests that exercise all IO ports.
    *
    * Chiseltest coverage utilities are used to achieve this. Coverage data is
    * stored in a Map. This utility processes that map file, checking that all
    * ports are toggled and returns an error if there is a stuck-at port which
    * indicates that the test is not sufficiently robust.
    *
    * The full report is written to the doc directory as part of the standard
    * deliverables.
    *
    * {{{
    * val result = checkCoverage(myCovMap,"cov.rpt")
    * }}}
    *
    * @param coverage
    *   the coverage Map containing coverage data
    * @param file
    *   the name of the file to write the coverage report
    * @return
    *   whether the coverage passed or failed
    */
  def checkCoverage(coverage: Map[String, Long], file: String): Boolean = {
    val cov            = new File(file)
    val covFile        = new PrintWriter(cov)
    val numTicks       = coverage("dut.tick")
    val netCoverage    = coverage.view.filterKeys(_ != "dut.tick").toMap
    val sortedCoverage = ListMap(netCoverage.toSeq.sortBy(_._1): _*).toMap
    var stuckAtOne     = false
    var stuckAtZero    = false
    val separator      = "-" * 80
    covFile.write(separator + "\n")
    covFile.write("%\t\t\t\t\t\t\t\t\tCount\t\t\t\tCoverage Point \n")
    covFile.write(separator + "\n")
    sortedCoverage.keys.foreach((coverPoint) => {
      val toggleCount = sortedCoverage(coverPoint)
      val togglePct   = toggleCount.toDouble / numTicks * 100
      covFile.write(
        f"${togglePct}%1.2f\t\t\t\t\t ${toggleCount}%8s\t\t\t\t${coverPoint}\n"
      )
      if (toggleCount == 0) {
        covFile.write(s"${coverPoint} is stuck at 0 \n")
        stuckAtZero = true
      }
      if (toggleCount == numTicks) {
        covFile.write(s"${coverPoint} is stuck at 1 \n")
        stuckAtOne = true
      }
    })
    covFile.write(separator + "\n")
    covFile.write(LocalDateTime.now.toString + "\n")
    covFile.close()
    return (stuckAtZero | stuckAtOne)
  }

  /** Return a random data word of arbitrary length
    *
    * Built-in scala random number generators do not work for generating random
    * numbers for words that are of a length not divisible by 4. This utility
    * returns a randomized bit vector where every bit is randomized.
    *
    * {{{
    * val myData = randData(19) // return a 19-bit word of random data
    * }}}
    *
    * @param width
    *   the coverage Map containing coverage data
    * @return
    *   a word with random data
    */
  def randData(width: Int): UInt = {

  // format: off
  /** Generate hex choices that can be randomized for creating data
    * patterns. The function returns fewer choices when there is data is
    * less than a full nibble as in the example below with dataWidth =
    * 13
    *
    *                 12 11 10  9  8  7  6  5  4  3  2  1  0  
    * full nibbles       ----------- ----------- ----------- 
    * 1 extra bit     --
    *
    * In this case, bit 12 can only take a 0/1 value because it is not
    * large enough to hold any other hex value.
    */
    // format: on
    def getHexString(x: Int): String = {
      val hexStringRem0 = "0123456789abcdef" // no extra bits
      val hexStringRem1 = "01"               // one extra bit
      val hexStringRem2 = "0123"             // two extra bits
      val hexStringRem3 = "01234567"         // three extra bits
      return x match {
        case 0 => hexStringRem0
        case 1 => hexStringRem1
        case 2 => hexStringRem2
        case 3 => hexStringRem3
      }
    }

    /** Determine whether a partial nibble should be generated */
    def partialNibble(x: Int): Int = {
      if (x == 0) { return 0 }
      else { return 1 }
    }

    val numNibbles      = width / 4
    val numLeftOverBits = width % 4

    // format: off
    /** Generate two sets of random strings used for generation of the
      * randomized data. One used for full nibbles the other for partial
      * nibbles.
      * 
      * datawidth = 13
      * randFullNibble = "abc"
      * randPartialNibble = "1"
      */
      // format: on

    val randFullNibble    = getHexString(0)
    val randPartialNibble = getHexString(numLeftOverBits)

    // format: off
    /** Assemble the test data word by creating two Seqs. One consists of
      * full nibbles, the second is for the last partial nibble. The
      * partial nibble value is prepended to the full list of nibbles.
      * 
      * fullNibbleSeq = Seq[List[String]] = List(List(a, b, c))
      * partialNibbleSeq = Seq[List[String]] = List(List(1))
      * assembleSeq = Seq[List[String]] = List(List(1,a,b,c))
      */
    // format: on

    val fullNibbleSeq = Seq.fill(width / 4) {
      Random.shuffle(randFullNibble).head
    }
    val partialNibbleSeq = Seq.fill(partialNibble(numLeftOverBits)) {
      Random.shuffle(randPartialNibble).head
    }
    val assembledSeq = partialNibbleSeq ++ fullNibbleSeq

    // Create a hex BigInt and cast it to UInt
    val randData = BigInt(assembledSeq.mkString, 16).U

    return randData
  }

}
