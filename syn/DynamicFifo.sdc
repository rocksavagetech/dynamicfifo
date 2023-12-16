
    create_clock -period 5.0 -waveform {0 2.5} -name clock
    set_input_delay -clock clock 1.0 {reset}
    set_input_delay -clock clock 1.0 {io_pop}
    set_input_delay -clock clock 1.0 {io_push}
    set_input_delay -clock clock 1.0 {io_dataIn}
    set_input_delay -clock clock 1.0 {io_almostFullLevel}
    set_input_delay -clock clock 1.0 {io_almostEmptyLevel}
    set_input_delay -clock clock 1.0 {io_ramDataOut}
    set_output_delay -clock clock 1.0 {io_dataOut}
    set_output_delay -clock clock 1.0 {io_empty}
    set_output_delay -clock clock 1.0 {io_full}
    set_output_delay -clock clock 1.0 {io_almostEmpty}
    set_output_delay -clock clock 1.0 {io_almostFull}
    set_output_delay -clock clock 1.0 {io_ramDataIn}
    set_output_delay -clock clock 1.0 {io_ramWriteEnable}
    set_output_delay -clock clock 1.0 {io_ramReadEnable}
  