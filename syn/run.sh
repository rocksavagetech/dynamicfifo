#!/bin/bash
rm *.log
yosys -Qv 1 -l DynamicFifo.log DynamicFifo.tcl  
