import chisel3._
import chisel3.util._
import CPU_Config._
object DCache_Config{
    val INDEX_WIDTH = 6
    val INDEX_DEPTH = 1 << INDEX_WIDTH

    val OFFSET_WIDTH = 6
    val OFFSET_DEPTH = 1 << OFFSET_WIDTH

    val TAG_WIDTH = 32 - INDEX_WIDTH - OFFSET_WIDTH

    val FROM_CMEM = 0.U(1.W)
    val FROM_RBUF = 1.U(1.W)

    val FROM_PIPE = 0.U(1.W)
    val FROM_SEG  = 1.U(1.W)
}

class DCache_IO extends Bundle{
    // EX stage
    val addr_RF         = Input(UInt(32.W))
    val mem_type_RF     = Input(UInt(5.W))
    val wdata_RF        = Input(UInt(32.W))
    val store_cmt_RF    = Input(Bool())
    val rob_index_EX    = Input(UInt(log2Ceil(ROB_NUM).W))
    // MEM stage
    val cache_miss_MEM  = Output(Bool())
    val rdata_MEM       = Output(UInt(32.W))

    // uncache cmt
    val rob_index_CMT   = Input(UInt(log2Ceil(ROB_NUM).W))

    // control
    val stall           = Input(Bool())
    val flush           = Input(Bool())
    val has_store       = Output(Bool())

    // for AXI arbiter
    val d_araddr        = Output(UInt(32.W))
    val d_rvalid        = Output(Bool())
    val d_rready        = Input(Bool())
    val d_rdata         = Input(UInt(32.W))
    val d_rlast         = Input(Bool())
    val d_rsize         = Output(UInt(3.W))
    val d_rburst        = Output(UInt(2.W))
    val d_rlen          = Output(UInt(8.W))

    val d_awaddr        = Output(UInt(32.W))
    val d_wdata         = Output(UInt(32.W))
    val d_wvalid        = Output(Bool())
    val d_wready        = Input(Bool())
    val d_wlast         = Output(Bool())
    val d_wstrb         = Output(UInt(4.W))
    val d_wsize         = Output(UInt(3.W))
    val d_wburst        = Output(UInt(2.W))
    val d_wlen          = Output(UInt(8.W))
    val d_bvalid        = Input(Bool())
    val d_bready        = Output(Bool())

    // for stat
    val commit_dcache_visit    = Output(Bool())
    val commit_dcache_miss     = Output(Bool())
}
import DCache_Config._
class DCache extends Module{
    val io = IO(new DCache_IO)
    val stall = io.stall

    // address decode EX
    val addr_RF             = io.addr_RF
    val tag_RF              = addr_RF(31, 32-TAG_WIDTH)
    val index_RF            = addr_RF(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_RF           = addr_RF(OFFSET_WIDTH-1, 0)
           

    // EX-TC SegReg
    val addr_reg_RF_EX      = RegInit(0.U(32.W))
    val mem_type_reg_RF_EX  = RegInit(0.U(5.W))
    val wdata_reg_RF_EX     = RegInit(0.U(32.W))
    val store_cmt_reg_RF_EX = RegInit(false.B)

    // TC Stage
    val tagv        = VecInit(Seq.fill(2)(Module(new xilinx_simple_dual_port_1_clock_ram_write_first(TAG_WIDTH+1, INDEX_DEPTH)).io))
    val tag_r_EX    = VecInit.tabulate(2)(i => tagv(i).doutb(TAG_WIDTH-1, 0))
    val valid_r_EX  = VecInit.tabulate(2)(i => tagv(i).doutb(TAG_WIDTH))
    
    // decode
    val addr_EX     = addr_reg_RF_EX
    val tag_EX      = addr_EX(31, 32-TAG_WIDTH)
    val index_EX    = addr_EX(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_EX   = addr_EX(OFFSET_WIDTH-1, 0)

    // TC-MEM SegReg
    val addr_reg_EX_MEM     = RegInit(0.U(32.W))
    val mem_type_reg_EX_MEM = RegInit(0.U(5.W))
    val wdata_reg_EX_MEM    = RegInit(0.U(32.W))
    val uncache_reg_EX_MEM  = RegInit(false.B)
    val rob_index_EX_MEM    = RegInit(0.U(log2Ceil(ROB_NUM).W))
    val hit_reg_EX_MEM      = RegInit(0.U(2.W))
    val tag_reg_EX_MEM      = RegInit(VecInit(Seq.fill(2)(0.U(TAG_WIDTH.W))))

    // MEM Stage
    val cmem            = VecInit(Seq.fill(2)(Module(new xilinx_simple_dual_port_byte_write_1_clock_ram_write_first(OFFSET_DEPTH, 8, INDEX_DEPTH)).io))
    val addr_MEM        = addr_reg_EX_MEM
    val mem_type_MEM    = mem_type_reg_EX_MEM
    val wdata_MEM       = WireDefault(wdata_reg_EX_MEM)

    val cache_miss_MEM  = WireDefault(false.B)
    val data_sel        = WireDefault(FROM_RBUF)
    val d_rvalid        = WireDefault(false.B)
    val addr_sel        = WireDefault(FROM_PIPE)

    // mem we
    val tagv_we_EX      = WireDefault(VecInit(Seq.fill(2)(false.B)))
    val cmem_we_MEM     = WireDefault(VecInit(Seq.fill(2)(0.U(OFFSET_DEPTH.W))))

    val flush_RF_EX     = RegInit(false.B)
    val flush_EX_MEM    = RegInit(false.B)

    // addr decode
    val tag_MEM         = addr_MEM(31, 32-TAG_WIDTH)
    val index_MEM       = addr_MEM(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_MEM      = addr_MEM(OFFSET_WIDTH-1, 0)
    val uncache_MEM     = uncache_reg_EX_MEM
    val tag_r_MEM       = tag_reg_EX_MEM

    // lru
    val lru_mem         = RegInit(VecInit(Seq.fill(INDEX_DEPTH)(0.U(1.W))))
    val lru_sel         = lru_mem(index_MEM)
    val lru_miss_upd    = WireDefault(false.B)
    val lru_hit_upd     = WireDefault(false.B)

    // ret buf
    val ret_buf         = RegInit(0.U((8*OFFSET_DEPTH).W))

    // write_buf
    val wrt_buf         = RegInit(0.U((8*OFFSET_DEPTH+32).W))
    val wbuf_we         = WireDefault(false.B)

    // dirty table
    val dirty_table     = RegInit(VecInit(Seq.fill(2)(VecInit(Seq.fill(INDEX_DEPTH)(false.B)))))
    val dirty_we        = WireDefault(false.B)
    val dirty_clean     = WireDefault(false.B)
    val is_dirty        = dirty_table(lru_sel)(index_MEM)

    // wfsm control
    val wfsm_en         = WireDefault(false.B)
    val wfsm_reset      = WireDefault(false.B)
    val wrt_finish      = WireDefault(false.B)
    val d_wvalid        = WireDefault(false.B)
    val d_wlast         = WireDefault(false.B)
    val d_bready        = WireDefault(false.B)

    // stat
    val dcache_miss     = WireDefault(false.B)
    val dcache_visit    = WireDefault(false.B)

    // EX Stage
    for(i <- 0 until 2){
        tagv(i).addra   := index_MEM
        tagv(i).addrb   := Mux(addr_sel === FROM_PIPE, index_RF, index_EX)
        tagv(i).dina    := true.B ## tag_MEM
        tagv(i).clka    := clock
        tagv(i).wea     := tagv_we_EX(i)
    }
    for(i <- 0 until 2){
        cmem(i).addra   := index_MEM
        cmem(i).addrb   := Mux(addr_sel === FROM_PIPE, index_EX, index_MEM)
        cmem(i).dina    := wdata_MEM
        cmem(i).clka    := clock
        cmem(i).wea     := cmem_we_MEM(i)
    }
    /* hit logic */
    val hit_EX          = VecInit.tabulate(2)(i => valid_r_EX(i) && tag_r_EX(i) === tag_EX).asUInt

    // EX-MEM SegReg
    when(!(stall || cache_miss_MEM)){
        addr_reg_RF_EX      := io.addr_RF
        mem_type_reg_RF_EX  := io.mem_type_RF
        wdata_reg_RF_EX     := io.wdata_RF
        flush_RF_EX         := io.flush
        store_cmt_reg_RF_EX := io.store_cmt_RF
    }
    when(io.flush){
        flush_RF_EX         := true.B
    }
    
    // EX-MEM SegReg
    val uncache_EX   = addr_reg_RF_EX(31, 24) =/= 0x1c.U
    when(!(stall || cache_miss_MEM)){
        addr_reg_EX_MEM     := addr_reg_RF_EX
        mem_type_reg_EX_MEM := Mux(mem_type_reg_RF_EX(3) || uncache_EX || store_cmt_reg_RF_EX, mem_type_reg_RF_EX, 0.U)
        wdata_reg_EX_MEM    := wdata_reg_RF_EX
        uncache_reg_EX_MEM  := uncache_EX
        rob_index_EX_MEM    := io.rob_index_EX
        hit_reg_EX_MEM      := hit_EX
        tag_reg_EX_MEM      := tag_r_EX
        flush_EX_MEM        := flush_RF_EX
    }
    when(io.flush){
        flush_EX_MEM        := true.B
    }

    // MEM Stage
    val hit_MEM         = hit_reg_EX_MEM
    val hit_index_MEM   = OHToUInt(hit_MEM)
    val cache_hit_MEM   = hit_MEM.orR
    
    /* rdata logic */
    val block_offset    = offset_MEM ## 0.U(3.W)
    val cmem_rdata_MEM  = (cmem(hit_index_MEM).doutb >> block_offset)(31, 0)
    val rbuf_rdata_MEM  = Mux(uncache_MEM, ret_buf(8*OFFSET_DEPTH-1, 8*OFFSET_DEPTH-32), (ret_buf >> block_offset)(31, 0))
    val rdata_MEM_temp  = Mux(data_sel === FROM_RBUF, rbuf_rdata_MEM, cmem_rdata_MEM)

    val highest_index   = (1.U << mem_type_MEM(1, 0)) ## 0.U(3.W)
    val highest_mask    = (UIntToOH(highest_index)(31, 0) - 1.U)(31, 0)
    val rmask           = highest_mask
    val rdata_MEM       = (rmask & rdata_MEM_temp)

    /* write logic */
    val wmask           = Mux(mem_type_MEM(3), 0.U((8*OFFSET_DEPTH).W), ((0.U((8*OFFSET_DEPTH-32).W) ## highest_mask) << block_offset))
    val wdata_refill    = ((0.U((8*OFFSET_DEPTH-32).W) ## wdata_reg_EX_MEM) << block_offset)
    wdata_MEM           := (wmask & wdata_refill) | (~wmask & ret_buf)
    val wmask_byte      = VecInit.tabulate(OFFSET_DEPTH)(i => wmask(8*i)).asUInt

    /* return buffer update logic */
    when(io.d_rready){
        ret_buf := Cat(io.d_rdata, ret_buf(8*OFFSET_DEPTH-1, 32))
    }

    /* lru logic */
    when(lru_hit_upd){
        lru_mem(index_MEM) := !hit_index_MEM
    }.elsewhen(lru_miss_upd){
        lru_mem(index_MEM) := !lru_sel
    }

    /* dirty table logic */
    val is_store_MEM = mem_type_MEM(4)
    val is_load_MEM = mem_type_MEM(3)
    when(dirty_we){
        val write_way = Mux(cache_hit_MEM, hit_index_MEM, lru_sel)
        dirty_table(write_way)(index_MEM) := true.B
    }.elsewhen(dirty_clean){
        dirty_table(lru_sel)(index_MEM) := false.B
    }

    /* write buffer */
    when(wbuf_we){
        when(uncache_MEM){
            wrt_buf := 0.U((8*OFFSET_DEPTH-32).W) ## wdata_reg_EX_MEM ## addr_MEM
        }.otherwise{
            wrt_buf := cmem(lru_sel).doutb ## tag_r_MEM(lru_sel) ## addr_MEM(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH) ## 0.U(OFFSET_WIDTH.W)
        }
    }.elsewhen(io.d_wready && io.d_wvalid){
        wrt_buf := 0.U(32.W) ## wrt_buf(8*OFFSET_DEPTH+32-1, 64) ## wrt_buf(31, 0)
    }
    
    /* read state machine */
    val s_idle :: s_miss :: s_refill :: s_wait :: s_hold :: Nil = Enum(5)
    val state = RegInit(s_idle)

    switch(state){
        is(s_idle){
            // has req
            when(mem_type_MEM(4, 3).orR){
                when(uncache_MEM){
                    state               := s_hold
                    cache_miss_MEM      := true.B
                    addr_sel            := FROM_SEG
                }.otherwise{
                    state                       := Mux(cache_hit_MEM, s_idle, s_miss)
                    cache_miss_MEM              := !cache_hit_MEM
                    lru_hit_upd                 := cache_hit_MEM
                    data_sel                    := FROM_CMEM
                    cmem_we_MEM(hit_index_MEM)  := Mux(is_store_MEM && cache_hit_MEM, wmask_byte, 0.U)
                    dirty_we                    := is_store_MEM
                    wbuf_we                     := !cache_hit_MEM
                    wfsm_en                     := !cache_hit_MEM
                    addr_sel                    := FROM_PIPE
                    dcache_visit                := true.B
                    dcache_miss                 := !cache_hit_MEM
                }
            }
        }
        is(s_miss){
            d_rvalid            := true.B
            cache_miss_MEM      := true.B
            state               := Mux(io.d_rready && io.d_rlast, Mux(uncache_MEM, s_wait, s_refill), s_miss)
            addr_sel            := FROM_SEG
        }
        is(s_refill){
            state                   := s_wait
            cache_miss_MEM          := true.B
            lru_miss_upd            := true.B
            tagv_we_EX(lru_sel)     := true.B
            cmem_we_MEM(lru_sel)    := Fill(OFFSET_DEPTH, 1.U(1.W))
            dirty_clean             := is_load_MEM
            dirty_we                := is_store_MEM
            addr_sel                := FROM_SEG
        }
        is(s_wait){
            addr_sel            := Mux(wrt_finish, FROM_PIPE, FROM_SEG)
            state               := Mux(wrt_finish, s_idle, s_wait)
            wfsm_reset          := true.B
            cache_miss_MEM      := !wrt_finish
        }
        is(s_hold){
            val confirm_exec    = io.rob_index_CMT === rob_index_EX_MEM
            addr_sel            := Mux(flush_EX_MEM, FROM_PIPE, FROM_SEG)
            state               := Mux(flush_EX_MEM, s_idle, Mux(confirm_exec, Mux(is_store_MEM, s_wait, s_miss), s_hold))
            cache_miss_MEM      := !flush_EX_MEM
            wfsm_reset          := flush_EX_MEM
            wfsm_en             := confirm_exec && !flush_EX_MEM
            wbuf_we             := confirm_exec && !flush_EX_MEM
        }
    }

    /* write fsm */
    val wrt_count         = RegInit(0.U(8.W))
    val wrt_count_reset   = WireDefault(false.B)
    val wrt_num           = Mux(uncache_MEM, 0.U, (OFFSET_DEPTH / 4 - 1).U)

    when(wrt_count_reset){
        wrt_count := wrt_num
    }.elsewhen(io.d_wvalid && io.d_wready){
        wrt_count := wrt_count - 1.U
    }

    val w_idle :: w_write :: w_finish :: Nil = Enum(3)
    val wrt_state = RegInit(w_idle)

    switch(wrt_state){
        is(w_idle){
            when(wfsm_en){
                when(uncache_MEM){
                    wrt_state       := Mux(is_store_MEM, w_write, w_finish)
                }.otherwise{
                    wrt_state       := Mux(is_dirty, w_write, w_finish)
                }
                wrt_count_reset := true.B
            }
        }
        is(w_write){
            wrt_state    := Mux(io.d_bvalid, w_finish, w_write)
            d_wvalid     := !wrt_count.andR
            d_wlast      := wrt_count === 0.U
            d_bready     := true.B
        }
        is(w_finish){
            wrt_state    := Mux(wfsm_reset && !stall, w_idle, w_finish)
            wrt_finish   := !stall
        }
    }


    // output
    io.has_store        := io.mem_type_RF(4) || mem_type_reg_RF_EX(4)
    io.cache_miss_MEM   := cache_miss_MEM
    io.rdata_MEM        := rdata_MEM
    io.d_araddr         := Mux(uncache_MEM, addr_MEM, addr_MEM(31, OFFSET_WIDTH) ## 0.U(OFFSET_WIDTH.W))
    io.d_rvalid         := d_rvalid
    io.d_rsize          := Mux(uncache_MEM, mem_type_MEM(1, 0), 2.U)
    io.d_rburst         := 1.U
    io.d_rlen           := Mux(uncache_MEM, 0.U, (OFFSET_DEPTH / 4 - 1).U)

    io.d_awaddr         := wrt_buf(31, 0)
    io.d_wdata          := wrt_buf(63, 32)
    io.d_wvalid         := d_wvalid
    io.d_wlast          := d_wlast
    io.d_wstrb          := Mux(uncache_MEM, (UIntToOH(UIntToOH(mem_type_MEM(1, 0))) - 1.U)(3, 0), Fill(4, 1.U(1.W)))
    io.d_wsize          := Mux(uncache_MEM, mem_type_MEM(1, 0), 2.U)
    io.d_wburst         := 1.U
    io.d_wlen           := wrt_num
    io.d_bready         := d_bready

    io.commit_dcache_miss   := dcache_miss
    io.commit_dcache_visit  := dcache_visit
    
}