package stack

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage
import java.nio.file.Paths


//CODE STARTS




//CODE ENDS

// Object for Verilog generation
object SVGen extends App {
  val out = Paths.get(
    "out",
    this.getClass
      .getName
      .stripSuffix("$")
  ).toString

  new ChiselStage().emitSystemVerilog(
    new StackModule(args(0).toInt, args(1).toInt),
    Array("--target-dir", out)
  )
}
