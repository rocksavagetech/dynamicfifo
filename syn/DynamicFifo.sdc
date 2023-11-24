create_clock -period 5.000 -waveform {0.000 2.500} -name clock
set_input_delay -clock clock 0 {reset}
set_input_delay -clock clock 0 {io_pop}
set_input_delay -clock clock 0 {io_push}
set_input_delay -clock clock 0 {io_dataIn}
set_input_delay -clock clock 0 {io_almostFullLevel}
set_input_delay -clock clock 0 {io_almostEmptyLevel}
set_input_delay -clock clock 0 {io_ramDataOut}
set_output_delay -clock clock 2 {io_empty}
set_output_delay -clock clock 2 {io_full}
set_output_delay -clock clock 2 {io_almostEmpty}
set_output_delay -clock clock 2 {io_almostFull}
set_output_delay -clock clock 2 {io_ramDataIn}
set_output_delay -clock clock 2 {io_ramWriteEnable}
set_output_delay -clock clock 2 {io_ramReadEnable}
