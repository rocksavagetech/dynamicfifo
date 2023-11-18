set top "DynamicFifo"
set techLib "/home/tws/drive/Fault/Tech/nangate/NangateOpenCellLibrary_typical.lib"

yosys -import
# Read in all the Verilog files in the filelist
set f [open filelist.f]
while {[gets $f line] > -1} {
  read_verilog -sv $line
}
close $f

# Synthesize, optimize, write out netlist and report
hierarchy -check -top $top
synth
dfflibmap -liberty $techLib
abc -liberty $techLib
clean
write_verilog $top\_net.v
stat -liberty $techLib
