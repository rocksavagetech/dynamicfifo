
module simple_dual_one_clock (
  clk,
  read_enable,
  write_enable,
  read_address,
  write_address,
  read_data,
  write_data;
);

input clk;
input read_enable;
input write_enable;
input [ADDR_SIZE-1:0] read_address;
input [ADDR_SIZE-1:0] write_address;
input [DATA_SIZE-1:0] read_data;
input [DATA_SIZE-1:0] write_data;

reg [SIZE-1:0];

always @(posedge clk) begin
  if (write_enable)
    ram[write_address] <= data_in;
  end

assign data_out = ram[read_address]

endmodule