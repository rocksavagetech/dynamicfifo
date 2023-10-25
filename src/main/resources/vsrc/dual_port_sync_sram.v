module simple_dual_one_clock #(
    parameter DATA_WIDTH = 8,
    parameter ADDR_WIDTH = 4
) (
    clk,
    read_enable,
    write_enable,
    read_address,
    write_address,
    read_data,
    write_data
);

  input clk;
  input read_enable;
  input [(ADDR_WIDTH-1):0] read_address;
  output [(DATA_WIDTH-1):0] read_data;
  input write_enable;
  input [(ADDR_WIDTH-1):0] write_address;
  input [(DATA_WIDTH-1):0] write_data;

  reg [(DATA_WIDTH-1):0] ram[2**ADDR_WIDTH];

  always @(posedge clk) begin
    if (write_enable) ram[write_address] <= write_data;
  end

  assign read_data = ram[read_address];

endmodule
