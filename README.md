# DynamicFifo

A highly configurable FIFO or FIFO controller with dynamic status flags.

## Description

The DynamicFifo can be configured to generate either standalone FIFOs built
from flip-flops or a FIFO controller that can be connected to an external 
dual-port synchronous SRAM. The standalone configuration is appropriate for 
small FIFOs whereas larger FIFOs are more efficiently implemented with an 
external memory.

Four status flags are provided:
* Empty
* Full
* almostEmpty
* almostFull

The latter two can be programmed to arbitrary values during operation providing 
the ability for dynamic flow control.

## Getting Started

It is recommended that the user reads the DynamicFiFo Users Guide can be 
found in the ```./doc/user-guide``` directory.

### Dependencies

Testing requires several utilities in the chiselWare util package.

The following should be added to the user's ```build.sbt``` file:

```
"tech.rocksavage"   %% "chiselware" % "0.1.0",
"tech.rocksavage"   %% "chiselware" % "0.1.0" % "test" classifier "tests"```
```
The utilities are imported to the test program as follows:

```
import tech.rocksavage.chiselware.util.TestUtils.{randData, checkCoverage}
```
Other non-Chisel-related dependencies are for two other open-source tools 
that are needed (only) for running the included synthesis regression tests:

* **[Yosys](https://yosyshq.net/yosys/)** (version 0.9) A synthesis and optimization (using ABC) tool
* **[OpenSTA](https://github.com/The-OpenROAD-Project/OpenSTA)** (version 2.4.0) A static timing analysis tool

### Installation

There are no special installation requirements. The code can be cloned in any 
directory for standalone use.


### Generating Verilog RTL

To generate an example configuration of SystemVerilog RTL, a helper app can be 
found in the main class file (DynamicFifo.scala) and executed as follows:

```
$ sbt
sbt:dynamicfifo>
sbt:dynamicfifo> runMain tech.rocksavage.chiselware.DynamicFifo.Main
```

The RTL will be generated in the ```./generated``` directory.

### Running a Simulation  

Multiple options are available for running simulations:

* iVerilog (open-source Verilog simulator)
* VCS (commercial simulator from Synopsys)
* Verilator (open source compiled Verilog simulator) 

An exhaustive constrained-random verilog test is included and can be executed 
as follows:

```
$ sbt
sbt:DynamicFifo>
sbt:DynamicFifo> test
```

### Synthesis

DynamicFifo is a DFT-clean, fully synthesizable core. 

A ```.sdc``` file is generated together with the RTL code.  Synthesis scripts 
for Yosys are included in the ```./syn``` directory and can be easily ported to 
commercial synthesis tools. Static timing analysis is also performed using 
OpenSTA.

Included also is the Nangate 45nm technology library to allow users to run
included synthesis regressions out-of-the-box and later change to their 
technology library of choice.

## Authors

Warren Savage
[@twsavage59]

## Version History

* 0.1
    * Initial Release