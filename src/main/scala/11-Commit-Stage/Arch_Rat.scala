import chisel3._
import chisel3.util._
import RAT._
object ARCH_RAT_Func{
    def Valid_Write_First_Read(cmt_en: Vec[Bool], rd_valid_cmt: Vec[Bool], prd_cmt: Vec[UInt], pprd_cmt: Vec[UInt], arat: Vec[rat_t], rindex: Int) : Bool = {
        val prd_wf = Cat(
                    rindex.U === prd_cmt(3) && cmt_en(3) && rd_valid_cmt(3),
                    rindex.U === prd_cmt(2) && cmt_en(2) && rd_valid_cmt(2),
                    rindex.U === prd_cmt(1) && cmt_en(1) && rd_valid_cmt(1),
                    rindex.U === prd_cmt(0) && cmt_en(0) && rd_valid_cmt(0)
                    )

        val pprd_wf = Cat(
                    rindex.U === pprd_cmt(3) && cmt_en(3) && rd_valid_cmt(3),
                    rindex.U === pprd_cmt(2) && cmt_en(2) && rd_valid_cmt(2),
                    rindex.U === pprd_cmt(1) && cmt_en(1) && rd_valid_cmt(1),
                    rindex.U === pprd_cmt(0) && cmt_en(0) && rd_valid_cmt(0)
                    )
        Mux(prd_wf.orR, true.B, Mux(pprd_wf.orR, false.B, arat(rindex).valid))
    }
}
import ARCH_RAT_Func._
class Arch_Rat_IO extends Bundle {
    // for commit 
    val cmt_en          = Input(Vec(4, Bool()))
    val rd_cmt          = Input(Vec(4, UInt(5.W)))
    val prd_cmt         = Input(Vec(4, UInt(7.W)))
    val pprd_cmt        = Input(Vec(4, UInt(7.W)))
    val rd_valid_cmt    = Input(Vec(4, Bool()))
    val predict_fail    = Input(Bool())

    // for reg rename
    val arch_rat        = Output(Vec(80, UInt(1.W)))
    val head_arch       = Output(Vec(4, UInt(5.W)))
}

class Arch_Rat extends Module {
    val io = IO(new Arch_Rat_IO)

    val arat = RegInit(VecInit(Seq.fill(80)(0.U.asTypeOf(new rat_t))))
    val arat_next = Wire(Vec(80, new rat_t))


    val head = RegInit(VecInit(1.U(5.W), 1.U(5.W), 1.U(5.W), 1.U(5.W)))
    val head_next = Wire(Vec(4, UInt(5.W)))
    val head_sel = RegInit(0.U(2.W))

    arat_next := arat
    for(i <- 0 until 4){
        when(io.rd_valid_cmt(i) && io.cmt_en(i)){
            arat_next(io.pprd_cmt(i)).valid := false.B
            arat_next(io.prd_cmt(i)).lr := io.rd_cmt(i)
            arat_next(io.prd_cmt(i)).valid := true.B
        }
    }
    arat := arat_next

    val cmt_en = io.cmt_en.asUInt.orR
    head_next := head
    for(i <- 0 until 4){
        head_next(head_sel+i.U) := Mux(head(head_sel+i.U) + (io.cmt_en(i) && io.rd_valid_cmt(i)) >= 20.U, 0.U, head(head_sel+i.U) + (io.cmt_en(i) && io.rd_valid_cmt(i)))
    }
    head := head_next
    head_sel := Mux(io.predict_fail, 0.U, head_sel + PopCount(io.cmt_en))

    for(i <- 0 until 80){
        io.arch_rat(i) := arat_next(i).valid
    }
    for(i <- 0 until 4){
        io.head_arch(i) := head_next(i)
    }
}