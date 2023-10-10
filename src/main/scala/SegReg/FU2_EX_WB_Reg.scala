import chisel3._
import chisel3.util._
import Inst_Pack._

class FU2_EX_WB_Reg extends Module {
    val io = IO(new Bundle {
        val flush = Input(Bool())
        val stall = Input(Bool())
        val inst_pack_EX = Input(new inst_pack_t)
        val alu_out_EX = Input(UInt(32.W))
        val predict_fail_EX = Input(Bool())
        val branch_target_EX = Input(UInt(32.W))
        val inst_valid_EX = Input(Bool())
        
        val inst_pack_WB = Output(new inst_pack_t)
        val alu_out_WB = Output(UInt(32.W))
        val predict_fail_WB = Output(Bool())
        val branch_target_WB = Output(UInt(32.W))
        val inst_valid_WB = Output(Bool())
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_t))
    val alu_out_reg = RegInit(0.U(32.W))
    val predict_fail_reg = RegInit(false.B)
    val branch_target_reg = RegInit(0.U(32.W))
    val inst_valid_reg = RegInit(false.B)

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_t)
        alu_out_reg := 0.U
        predict_fail_reg := false.B
        branch_target_reg := 0.U
        inst_valid_reg := false.B
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX
        alu_out_reg := io.alu_out_EX
        predict_fail_reg := io.predict_fail_EX
        branch_target_reg := io.branch_target_EX
        inst_valid_reg := io.inst_valid_EX
    }

    io.inst_pack_WB := inst_pack_reg
    io.alu_out_WB := alu_out_reg
    io.predict_fail_WB := predict_fail_reg
    io.branch_target_WB := branch_target_reg
    io.inst_valid_WB := inst_valid_reg

}