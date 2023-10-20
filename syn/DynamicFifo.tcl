set design "DynamicFifo"
yosys -import
read_verilog ../generated/$design.sv
hierarchy -check -top $design 
synth
clean
write_verilog $design\_net.v
