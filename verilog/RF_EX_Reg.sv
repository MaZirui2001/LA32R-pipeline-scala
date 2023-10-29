// Generated by CIRCT firtool-1.56.1
module RF_EX_Reg(
  input         clock,
                reset,
                io_flush,
  input  [6:0]  io_inst_pack_RF_prj,
                io_inst_pack_RF_prk,
  input         io_inst_pack_RF_rd_valid,
  input  [6:0]  io_inst_pack_RF_prd,
  input  [31:0] io_inst_pack_RF_imm,
  input  [4:0]  io_inst_pack_RF_alu_op,
  input  [1:0]  io_inst_pack_RF_alu_rs1_sel,
                io_inst_pack_RF_alu_rs2_sel,
  input  [3:0]  io_inst_pack_RF_br_type,
  input  [4:0]  io_inst_pack_RF_mem_type,
  input  [31:0] io_inst_pack_RF_pc,
  input  [5:0]  io_inst_pack_RF_rob_index,
  input         io_inst_pack_RF_predict_jump,
  input  [31:0] io_inst_pack_RF_pred_npc,
  input         io_inst_pack_RF_inst_valid,
  input  [31:0] io_src1_RF,
                io_src2_RF,
  output [6:0]  io_inst_pack_EX_prj,
                io_inst_pack_EX_prk,
  output        io_inst_pack_EX_rd_valid,
  output [6:0]  io_inst_pack_EX_prd,
  output [31:0] io_inst_pack_EX_imm,
  output [4:0]  io_inst_pack_EX_alu_op,
  output [1:0]  io_inst_pack_EX_alu_rs1_sel,
                io_inst_pack_EX_alu_rs2_sel,
  output [3:0]  io_inst_pack_EX_br_type,
  output [4:0]  io_inst_pack_EX_mem_type,
  output [31:0] io_inst_pack_EX_pc,
  output [5:0]  io_inst_pack_EX_rob_index,
  output        io_inst_pack_EX_predict_jump,
  output [31:0] io_inst_pack_EX_pred_npc,
  output        io_inst_pack_EX_inst_valid,
  output [31:0] io_src1_EX,
                io_src2_EX
);

  reg [6:0]  inst_pack_reg_prj;
  reg [6:0]  inst_pack_reg_prk;
  reg        inst_pack_reg_rd_valid;
  reg [6:0]  inst_pack_reg_prd;
  reg [31:0] inst_pack_reg_imm;
  reg [4:0]  inst_pack_reg_alu_op;
  reg [1:0]  inst_pack_reg_alu_rs1_sel;
  reg [1:0]  inst_pack_reg_alu_rs2_sel;
  reg [3:0]  inst_pack_reg_br_type;
  reg [4:0]  inst_pack_reg_mem_type;
  reg [31:0] inst_pack_reg_pc;
  reg [5:0]  inst_pack_reg_rob_index;
  reg        inst_pack_reg_predict_jump;
  reg [31:0] inst_pack_reg_pred_npc;
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
      inst_pack_reg_alu_op <= 5'h0;
      inst_pack_reg_alu_rs1_sel <= 2'h0;
      inst_pack_reg_alu_rs2_sel <= 2'h0;
      inst_pack_reg_br_type <= 4'h0;
      inst_pack_reg_mem_type <= 5'h0;
      inst_pack_reg_pc <= 32'h0;
      inst_pack_reg_rob_index <= 6'h0;
      inst_pack_reg_predict_jump <= 1'h0;
      inst_pack_reg_pred_npc <= 32'h0;
      inst_pack_reg_inst_valid <= 1'h0;
      src1_reg <= 32'h0;
      src2_reg <= 32'h0;
    end
    else begin
      if (io_flush) begin
        inst_pack_reg_prj <= 7'h0;
        inst_pack_reg_prk <= 7'h0;
        inst_pack_reg_prd <= 7'h0;
        inst_pack_reg_imm <= 32'h0;
        inst_pack_reg_alu_op <= 5'h0;
        inst_pack_reg_alu_rs1_sel <= 2'h0;
        inst_pack_reg_alu_rs2_sel <= 2'h0;
        inst_pack_reg_br_type <= 4'h0;
        inst_pack_reg_mem_type <= 5'h0;
        inst_pack_reg_pc <= 32'h0;
        inst_pack_reg_rob_index <= 6'h0;
        inst_pack_reg_pred_npc <= 32'h0;
        src1_reg <= 32'h0;
        src2_reg <= 32'h0;
      end
      else begin
        inst_pack_reg_prj <= io_inst_pack_RF_prj;
        inst_pack_reg_prk <= io_inst_pack_RF_prk;
        inst_pack_reg_prd <= io_inst_pack_RF_prd;
        inst_pack_reg_imm <= io_inst_pack_RF_imm;
        inst_pack_reg_alu_op <= io_inst_pack_RF_alu_op;
        inst_pack_reg_alu_rs1_sel <= io_inst_pack_RF_alu_rs1_sel;
        inst_pack_reg_alu_rs2_sel <= io_inst_pack_RF_alu_rs2_sel;
        inst_pack_reg_br_type <= io_inst_pack_RF_br_type;
        inst_pack_reg_mem_type <= io_inst_pack_RF_mem_type;
        inst_pack_reg_pc <= io_inst_pack_RF_pc;
        inst_pack_reg_rob_index <= io_inst_pack_RF_rob_index;
        inst_pack_reg_pred_npc <= io_inst_pack_RF_pred_npc;
        src1_reg <= io_src1_RF;
        src2_reg <= io_src2_RF;
      end
      inst_pack_reg_rd_valid <= ~io_flush & io_inst_pack_RF_rd_valid;
      inst_pack_reg_predict_jump <= ~io_flush & io_inst_pack_RF_predict_jump;
      inst_pack_reg_inst_valid <= ~io_flush & io_inst_pack_RF_inst_valid;
    end
  end // always @(posedge)
  assign io_inst_pack_EX_prj = inst_pack_reg_prj;
  assign io_inst_pack_EX_prk = inst_pack_reg_prk;
  assign io_inst_pack_EX_rd_valid = inst_pack_reg_rd_valid;
  assign io_inst_pack_EX_prd = inst_pack_reg_prd;
  assign io_inst_pack_EX_imm = inst_pack_reg_imm;
  assign io_inst_pack_EX_alu_op = inst_pack_reg_alu_op;
  assign io_inst_pack_EX_alu_rs1_sel = inst_pack_reg_alu_rs1_sel;
  assign io_inst_pack_EX_alu_rs2_sel = inst_pack_reg_alu_rs2_sel;
  assign io_inst_pack_EX_br_type = inst_pack_reg_br_type;
  assign io_inst_pack_EX_mem_type = inst_pack_reg_mem_type;
  assign io_inst_pack_EX_pc = inst_pack_reg_pc;
  assign io_inst_pack_EX_rob_index = inst_pack_reg_rob_index;
  assign io_inst_pack_EX_predict_jump = inst_pack_reg_predict_jump;
  assign io_inst_pack_EX_pred_npc = inst_pack_reg_pred_npc;
  assign io_inst_pack_EX_inst_valid = inst_pack_reg_inst_valid;
  assign io_src1_EX = src1_reg;
  assign io_src2_EX = src2_reg;
endmodule
