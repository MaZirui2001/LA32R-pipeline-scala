// Generated by CIRCT firtool-1.56.1
module Order_Select(
  input  [6:0]  io_insts_issue_inst_prj,
                io_insts_issue_inst_prk,
  input         io_insts_issue_inst_rd_valid,
  input  [6:0]  io_insts_issue_inst_prd,
  input  [31:0] io_insts_issue_inst_imm,
  input  [4:0]  io_insts_issue_inst_alu_op,
                io_insts_issue_inst_mem_type,
  input  [5:0]  io_insts_issue_inst_rob_index,
  input         io_issue_req,
                io_stall,
  output        io_issue_ack,
  output [6:0]  io_wake_preg,
                io_inst_issue_inst_prj,
                io_inst_issue_inst_prk,
  output        io_inst_issue_inst_rd_valid,
  output [6:0]  io_inst_issue_inst_prd,
  output [31:0] io_inst_issue_inst_imm,
  output [4:0]  io_inst_issue_inst_alu_op,
                io_inst_issue_inst_mem_type,
  output [5:0]  io_inst_issue_inst_rob_index,
  output        io_inst_issue_valid
);

  wire _io_issue_ack_output = ~io_stall & io_issue_req;
  assign io_issue_ack = _io_issue_ack_output;
  assign io_wake_preg =
    _io_issue_ack_output & io_insts_issue_inst_rd_valid ? io_insts_issue_inst_prd : 7'h0;
  assign io_inst_issue_inst_prj = _io_issue_ack_output ? io_insts_issue_inst_prj : 7'h0;
  assign io_inst_issue_inst_prk = _io_issue_ack_output ? io_insts_issue_inst_prk : 7'h0;
  assign io_inst_issue_inst_rd_valid =
    _io_issue_ack_output & io_insts_issue_inst_rd_valid;
  assign io_inst_issue_inst_prd = _io_issue_ack_output ? io_insts_issue_inst_prd : 7'h0;
  assign io_inst_issue_inst_imm = _io_issue_ack_output ? io_insts_issue_inst_imm : 32'h0;
  assign io_inst_issue_inst_alu_op =
    _io_issue_ack_output ? io_insts_issue_inst_alu_op : 5'h0;
  assign io_inst_issue_inst_mem_type =
    _io_issue_ack_output ? io_insts_issue_inst_mem_type : 5'h0;
  assign io_inst_issue_inst_rob_index =
    _io_issue_ack_output ? io_insts_issue_inst_rob_index : 6'h0;
  assign io_inst_issue_valid = _io_issue_ack_output;
endmodule

