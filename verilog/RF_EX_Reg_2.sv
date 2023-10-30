// Generated by CIRCT firtool-1.58.0
module RF_EX_Reg_2(
  input         clock,
                reset,
                io_flush,
  input  [6:0]  io_inst_pack_RF_prj,
                io_inst_pack_RF_prk,
  input         io_inst_pack_RF_rd_valid,
  input  [6:0]  io_inst_pack_RF_prd,
  input  [31:0] io_inst_pack_RF_imm,
  input  [5:0]  io_inst_pack_RF_rob_index,
  input  [4:0]  io_inst_pack_RF_mem_type,
  input         io_inst_pack_RF_inst_valid,
  input  [31:0] io_src1_RF,
                io_src2_RF,
  output [6:0]  io_inst_pack_EX_prj,
                io_inst_pack_EX_prk,
  output        io_inst_pack_EX_rd_valid,
  output [6:0]  io_inst_pack_EX_prd,
  output [31:0] io_inst_pack_EX_imm,
  output [5:0]  io_inst_pack_EX_rob_index,
  output [4:0]  io_inst_pack_EX_mem_type,
  output        io_inst_pack_EX_inst_valid,
  output [31:0] io_src1_EX,
                io_src2_EX
);

  reg [6:0]  inst_pack_reg_prj;
  reg [6:0]  inst_pack_reg_prk;
  reg        inst_pack_reg_rd_valid;
  reg [6:0]  inst_pack_reg_prd;
  reg [31:0] inst_pack_reg_imm;
  reg [5:0]  inst_pack_reg_rob_index;
  reg [4:0]  inst_pack_reg_mem_type;
  reg        inst_pack_reg_inst_valid;
  reg [31:0] src1_reg;
  reg [31:0] src2_reg;
  always @(posedge clock) begin
    if (reset) begin
      inst_pack_reg_prj <= 7'h0;
      inst_pack_reg_prk <= 7'h0;
      inst_pack_reg_rd_valid <= 1'h0;
      inst_pack_reg_prd <= 7'h0;
      inst_pack_reg_imm <= 32'h0;
      inst_pack_reg_rob_index <= 6'h0;
      inst_pack_reg_mem_type <= 5'h0;
      inst_pack_reg_inst_valid <= 1'h0;
      src1_reg <= 32'h0;
      src2_reg <= 32'h0;
    end
    else begin
      inst_pack_reg_prj <= io_flush ? 7'h0 : io_inst_pack_RF_prj;
      inst_pack_reg_prk <= io_flush ? 7'h0 : io_inst_pack_RF_prk;
      inst_pack_reg_rd_valid <= ~io_flush & io_inst_pack_RF_rd_valid;
      inst_pack_reg_prd <= io_flush ? 7'h0 : io_inst_pack_RF_prd;
      inst_pack_reg_imm <= io_flush ? 32'h0 : io_inst_pack_RF_imm;
      inst_pack_reg_rob_index <= io_flush ? 6'h0 : io_inst_pack_RF_rob_index;
      inst_pack_reg_mem_type <= io_flush ? 5'h0 : io_inst_pack_RF_mem_type;
      inst_pack_reg_inst_valid <= ~io_flush & io_inst_pack_RF_inst_valid;
      src1_reg <= io_flush ? 32'h0 : io_src1_RF;
      src2_reg <= io_flush ? 32'h0 : io_src2_RF;
    end
  end // always @(posedge)
  assign io_inst_pack_EX_prj = inst_pack_reg_prj;
  assign io_inst_pack_EX_prk = inst_pack_reg_prk;
  assign io_inst_pack_EX_rd_valid = inst_pack_reg_rd_valid;
  assign io_inst_pack_EX_prd = inst_pack_reg_prd;
  assign io_inst_pack_EX_imm = inst_pack_reg_imm;
  assign io_inst_pack_EX_rob_index = inst_pack_reg_rob_index;
  assign io_inst_pack_EX_mem_type = inst_pack_reg_mem_type;
  assign io_inst_pack_EX_inst_valid = inst_pack_reg_inst_valid;
  assign io_src1_EX = src1_reg;
  assign io_src2_EX = src2_reg;
endmodule

