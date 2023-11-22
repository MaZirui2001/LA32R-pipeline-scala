import chisel3._
import chisel3.util._
import RAT._
import PRED_Config._

class Arch_Rat_IO extends Bundle {
    // for commit 
    val cmt_en          = Input(Vec(4, Bool()))
    val prd_cmt         = Input(Vec(4, UInt(7.W)))
    val pprd_cmt        = Input(Vec(4, UInt(7.W)))
    val rd_valid_cmt    = Input(Vec(4, Bool()))
    val predict_fail    = Input(Bool())

    // for reg rename
    val arch_rat        = Output(Vec(85, UInt(1.W)))
    val head_arch       = Output(Vec(4, UInt(5.W)))

    // for ras
    val top_arch            = Output(UInt(4.W))
    val br_type_pred_cmt    = Input(UInt(2.W))
    val pred_update_en_cmt  = Input(Bool())
}

class Arch_Rat extends Module {
    val io = IO(new Arch_Rat_IO)

    val arat = RegInit(VecInit(Seq.fill(85)(false.B)))
    val arat_next = Wire(Vec(85, Bool()))


    val head = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val head_next = Wire(Vec(4, UInt(5.W)))
    val head_sel = RegInit(0.U(2.W))

    arat_next := arat
    for(i <- 0 until 4){
        when(io.rd_valid_cmt(i) && io.cmt_en(i)){
            arat_next(io.pprd_cmt(i)) := false.B
            arat_next(io.prd_cmt(i)) := true.B
        }
    }
    arat := arat_next

    val cmt_en = io.cmt_en.reduce(_||_)
    head_next := head
    for(i <- 0 until 4){
        head_next(head_sel+i.U) := Mux(head(head_sel+i.U) + (io.cmt_en(i) && io.rd_valid_cmt(i)) === 22.U, 0.U, head(head_sel+i.U) + (io.cmt_en(i) && io.rd_valid_cmt(i)))
    }
    head := head_next
    head_sel := Mux(io.predict_fail, 0.U, head_sel + PopCount(io.cmt_en))

    // ras
    val top = RegInit(0.U(4.W))
    val top_next = Wire(UInt(4.W))
    top_next := top
    when(io.br_type_pred_cmt === RET && io.pred_update_en_cmt){
        top_next := top - 1.U
    }.elsewhen((io.br_type_pred_cmt === BL || io.br_type_pred_cmt === ICALL) && io.pred_update_en_cmt){
        top_next := top + 1.U
    }
    top := top_next
    io.top_arch := top_next
    io.arch_rat := arat_next
    io.head_arch := head_next
}