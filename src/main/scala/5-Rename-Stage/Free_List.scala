import chisel3._
import chisel3.util._
// LUT: 435 FF: 416
class Free_List extends Module{
    val io = IO(new Bundle{
        val rd_valid            = Input(Vec(4, Bool()))
        val rename_en           = Input(Vec(4, Bool()))
        val alloc_preg          = Output(Vec(4, UInt(7.W)))
        val empty               = Output(Bool())

        val commit_en           = Input(Vec(4, Bool()))
        val commit_pprd_valid   = Input(Vec(4, Bool()))
        val commit_pprd         = Input(Vec(4, UInt(7.W)))

        val predict_fail        = Input(Bool())
        val head_arch           = Input(Vec(4, UInt(5.W)))
    })

    val free_list = Reg(Vec(4, Vec(20, UInt(7.W))))
    when(reset.asBool) {
        for(j <- 0 until 20){
            for(i <- 0 until 4){
                free_list(i)(j) := (j * 4 + i).asUInt
            }
        }
    }
    val head = RegInit(VecInit(1.U(5.W), 1.U(5.W), 1.U(5.W), 1.U(5.W)))
    val tail = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val tail_sel = RegInit(0.U(2.W))

    io.empty := (head(0) === tail(0)) | (head(1) === tail(1)) | (head(2) === tail(2)) | (head(3) === tail(3))

    for(i <- 0 until 4){
        when(io.predict_fail){
            head(i) := io.head_arch(i)
        }.elsewhen(!io.empty && io.rename_en(i)){
            head(i) := Mux(head(i) + io.rd_valid(i) >= 20.U, 0.U, head(i) + io.rd_valid(i))
        }
        when(io.commit_en(i)){
            tail(tail_sel+i.U) := Mux(tail(tail_sel+i.U) + io.commit_pprd_valid(i) >= 20.U, 0.U, tail(tail_sel+i.U) + io.commit_pprd_valid(i))
            when(io.commit_pprd_valid(i)){
                free_list(tail_sel+i.U)(tail(tail_sel+i.U)) := io.commit_pprd(i)
            }
            tail_sel := Mux(io.predict_fail, 0.U, tail_sel + PopCount(io.commit_en))
        }
    }
    for(i <- 0 until 4){
        io.alloc_preg(i) := free_list(i)(head(i))
    }


}
// object Free_List extends App{
//     emitVerilog(new Free_List, Array("-td", "verilog/"))
// }