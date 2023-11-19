import chisel3._
import circt.stage.ChiselStage

object CPU_Main extends App {
    ChiselStage.emitSystemVerilogFile(
        new CPU(0x1c000000), 
        Array("-td", "build/"),
        firtoolOpts = Array("-disable-all-randomization", 
                            "-strip-debug-info",
                            "-strip-fir-debug-info",
                            "-O=release",
                            "--ignore-read-enable-mem",
                            "--lower-memories",
                            "--lowering-options=disallowLocalVariables, explicitBitcast, disallowMuxInlining, disallowExpressionInliningInPorts, verifLabels",
                            "-o=verilog/",
                            "-split-verilog",
                            )
    )
}
// object Cache_Main extends App {
//     ChiselStage.emitSystemVerilogFile(
//         new Cache_Top, 
//         Array("-td", "build/"),
//         firtoolOpts = Array("-disable-all-randomization", 
//                             "-strip-debug-info",
//                             "-strip-fir-debug-info",
//                             "-O=release",
//                             "--ignore-read-enable-mem",
//                             "--lower-memories",
//                             "--lowering-options= disallowLocalVariables, explicitBitcast, mitigateVivadoArrayIndexConstPropBug, disallowMuxInlining",
//                             "-o=verilog/",
//                             "-split-verilog",
//                             )
//     )
// }


