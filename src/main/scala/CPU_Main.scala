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
                            "--lowering-options=noAlwaysComb, exprInEventControl, disallowPackedArrays, disallowLocalVariables, verifLabels, explicitBitcast, emitReplicatedOpsToHeader, locationInfoStyle=wrapInAtSquareBracket, disallowPortDeclSharing, printDebugInfo, disallowExpressionInliningInPorts, disallowMuxInlining, emitBindComments, omitVersionComment, caseInsensitiveKeywords",
                            "-o=verilog/",
                            "-split-verilog",
                            // "--lower-memories",
                            // "--lowering-options="
                            // "--preserve-aggregate=vec"
                            )
    )
}


// object Predict_Main extends App {
//     ChiselStage.emitSystemVerilogFile(
//         new Predict, 
//         Array("-td", "build/"),
//         firtoolOpts = Array("-disable-all-randomization", 
//                             "-strip-debug-info",
//                             "-strip-fir-debug-info",
//                             "-O=release",
//                             "--ignore-read-enable-mem",
//                             "--lower-memories",
//                             "--lowering-options=noAlwaysComb, exprInEventControl, disallowPackedArrays, disallowLocalVariables, verifLabels, explicitBitcast, emitReplicatedOpsToHeader, locationInfoStyle=wrapInAtSquareBracket, disallowPortDeclSharing, printDebugInfo, disallowExpressionInliningInPorts, disallowMuxInlining, emitBindComments, omitVersionComment, caseInsensitiveKeywords"
//                             // "--lower-memories",
//                             // "--lowering-options="
//                             // "--preserve-aggregate=vec"
//                             )
//     )
// }
