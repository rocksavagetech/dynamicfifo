# DynamicFifo

A highly configurable FIFO or FIFO controller with dynamic status flags.

## Description

The DynamicFifo can be configured to generate either standalone FIFO's built
from flip-flops or a FIFO controller that can be connected to an external 
dual-port synchronous SRAM. The standalone configuration is a appropriate for 
small FIFOs whereas larger FIFOs are more efficiently implemented with an 
external memory.

Four status flags are provided:
* Empty
* Full
* almostEmpty
* almostFull

The latter two can be programmed on-the-fly to arbitrary values providing the 
ability for dynamic flow control.

## Getting Started

It is recommended that the user reads the DynamicFiFo Users Guide that can be 
found in the ```./doc/pdf``` directory.

### Dependencies

None. 

### Installation

There are no special installation requirements. The code can be cloned in any 
directory for standalone use.


### Generating Verilog RTL

To generate the System Verilog RTL, a helper app can be found in the main class 
file (DynamicFifo.scala) and executed as follows:

```
$ sbt
sbt:DynamicFifo>
sbt:DynamicFifo> runMain chiselWare.DynamicFifo
```

The RTL will be generated in the ```./generated``` directory.

### Running a Simulation  

Multiple options are available for running simulations:

* iVerilog (open source Verilog simulator)
* VCS (commercial simulator from Synopsys)
* Verilator (open source compiled Verilog simulator) 

To run all the tests:

```
$ sbt
sbt:DynamicFifo>
sbt:DynamicFifo> testOnly chiselWare.TestDynamicFifo*
```

### Synthesis

DynamicFifo is a DFT-clean, fully synthesizable core. An ```.sdc``` file and synthesis 
scripts for Yosys is included in the ```./syn``` directory and can be easily 
ported to commercial synthesis tools.

## Authors

Warren Savage
[@twsavage59]

## Version History

* 0.1
    * Initial Release

