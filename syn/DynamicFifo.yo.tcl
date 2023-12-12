set top "DynamicFifo"
set techLib "../../syn/stdcells.lib"

yosys -import

# Read in all the Verilog files in the filelist
set f [open filelist.f]
while {[gets $f line] > -1} {
  read_verilog -sv $line
}
close $f

# Synthesize, optimize, write out netlist and report
hierarchy -check -top $top
synth -top $top
flatten
dfflibmap -liberty $techLib
abc -liberty $techLib
opt_clean -purge
write_verilog -noattr $top\_net.v
stat -liberty $techLib
