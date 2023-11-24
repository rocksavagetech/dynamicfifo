set top "DynamicFifo"

read_liberty ../../syn/stdcells.lib
read_verilog $top\_net.v
link_design $top
source ../../syn/$top.sdc
report_checks