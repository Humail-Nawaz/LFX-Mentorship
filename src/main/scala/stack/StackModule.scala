package stack

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage
import java.nio.file.Paths


//CODE STARTS
class StackModule(val dataWidth: Int, val len: Int) extends Module {
  val io = IO(new Bundle {
    val in         = Input(UInt(32.W))       // Full instruction
    val out        = Output(UInt(dataWidth.W))
    val underflow  = Output(Bool())
    val overflow   = Output(Bool())
    val isEmpty    = Output(Bool())
    val isFull     = Output(Bool())
    val popped     = Output(Bool())
    val peeked     = Output(Bool())
  })

  // Internal stack and pointer
  val stack = Reg(Vec(len, UInt(dataWidth.W)))
  val sp    = RegInit(0.U(log2Ceil(len + 1).W))  // stack pointer

  // Registered outputs for alignment with cocotb
  val outReg        = RegInit(0.U(dataWidth.W))
  val underflowReg  = RegInit(false.B)
  val overflowReg   = RegInit(false.B)
  val poppedReg     = RegInit(false.B)
  val peekedReg     = RegInit(false.B)

  // Output assignments
  io.out        := outReg
  io.underflow  := underflowReg
  io.overflow   := overflowReg
  io.isEmpty    := sp === 0.U
  io.isFull     := sp === len.U
  io.popped     := poppedReg
  io.peeked     := peekedReg

  // Reset logic
  when (reset.asBool) {
    sp := 0.U
    outReg := 0.U
    underflowReg := false.B
    overflowReg := false.B
    poppedReg := false.B
    peekedReg := false.B
  } .otherwise {
    // Decode instruction
    val opcode = io.in(6, 0)
    // Decode immediate safely
    val immRaw = io.in(31, 7)              // 25-bit immediate field
    val immZext = immRaw.zext().asUInt()   // Sign or zero extended
    val pushVal = Wire(UInt(dataWidth.W))
    pushVal := immZext

    val isPush = opcode === "b0100111".U
    val isPop  = opcode === "b1000011".U
    val isPeek = opcode === "b1000000".U

    // Clear flags every cycle unless set
    underflowReg := false.B
    overflowReg := false.B
    poppedReg := false.B
    peekedReg := false.B
    outReg := 0.U

    when (isPush) {
  when (io.isFull) {
    overflowReg := true.B
  } .otherwise {
    stack(sp) := pushVal
    sp := sp + 1.U
  }
 }.elsewhen (isPop) {
      when (sp === 0.U) {
        underflowReg := true.B
        outReg := 0.U
      } .otherwise {
        outReg := stack(sp - 1.U)
        sp := sp - 1.U
        poppedReg := true.B
      }

    } .elsewhen (isPeek) {
      when (sp === 0.U) {
        underflowReg := true.B
        outReg := 0.U
      } .otherwise {
        outReg := stack(sp - 1.U)
        peekedReg := true.B
      }
    }
  }
}
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

