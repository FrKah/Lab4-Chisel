
import chisel3._
import chisel3.util.HasBlackBoxPath

/// wrapper module to instantiate the SocAdapter blackbox
class PythonSocAdapterWrapper extends Module {
  val io = IO(new Bundle {
    val psel = Input(Bool())
    val penable = Input(Bool())
    val paddr = Input(UInt(32.W))
    val pwrite = Input(Bool())
    val pwdata = Input(UInt(32.W))
    val prdata = Output(UInt(32.W))
    val pready = Output(Bool())
    val pslverr = Output(Bool())

    val uart0_ctrl_en = Output(Bool())
    val uart0_ctrl_loopback = Output(Bool())
    val uart0_status_txEmpty = Input(Bool())
    val uart0_status_rxReady = Input(Bool())
    val uart0_data_txData = Output(UInt(8.W))
    val uart0_data_txData_trg = Output(Bool())
    val uart0_data_rxData = Input(UInt(8.W))
    val uart0_data_rxData_trg = Output(Bool())

    val gpio0_ctrl_en = Output(Bool())
    val gpio0_dir = Output(UInt(32.W))
    val gpio0_dataIn = Input(UInt(32.W))
    val gpio0_dataOut = Output(UInt(32.W))

    val gpio1_ctrl_en = Output(Bool())
    val gpio1_dir = Output(UInt(32.W))
    val gpio1_dataIn = Input(UInt(32.W))
    val gpio1_dataOut = Output(UInt(32.W))
  })
  val adapter = Module(new PythonSocAdapter)
  adapter.io.clock := clock
  adapter.io.reset := reset
  adapter.io.psel := io.psel
  adapter.io.penable := io.penable
  adapter.io.paddr := io.paddr
  adapter.io.pwrite := io.pwrite
  adapter.io.pwdata := io.pwdata
  io.prdata := adapter.io.prdata
  io.pready := adapter.io.pready
  io.pslverr := adapter.io.pslverr
  io.uart0_ctrl_en := adapter.io.uart0_ctrl_en
  io.uart0_ctrl_loopback := adapter.io.uart0_ctrl_loopback
  adapter.io.uart0_status_txEmpty := io.uart0_status_txEmpty
  adapter.io.uart0_status_rxReady := io.uart0_status_rxReady
  io.uart0_data_txData := adapter.io.uart0_data_txData
  io.uart0_data_txData_trg := adapter.io.uart0_data_txData_trg
  adapter.io.uart0_data_rxData := io.uart0_data_rxData
  io.uart0_data_rxData_trg := adapter.io.uart0_data_rxData_trg
  io.gpio0_ctrl_en := adapter.io.gpio0_ctrl_en
  io.gpio0_dir := adapter.io.gpio0_dir
  adapter.io.gpio0_dataIn := io.gpio0_dataIn
  io.gpio0_dataOut := adapter.io.gpio0_dataOut
  io.gpio1_ctrl_en := adapter.io.gpio1_ctrl_en
  io.gpio1_dir := adapter.io.gpio1_dir
  adapter.io.gpio1_dataIn := io.gpio1_dataIn
  io.gpio1_dataOut := adapter.io.gpio1_dataOut
}


/// blackbox for the generated SocAdapter
class PythonSocAdapter extends BlackBox with HasBlackBoxPath {

  override def desiredName: String = "soc_adapter"

  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())

    val psel = Input(Bool())
    val penable = Input(Bool())
    val paddr = Input(UInt(32.W))
    val pwrite = Input(Bool())
    val pwdata = Input(UInt(32.W))
    val prdata = Output(UInt(32.W))
    val pready = Output(Bool())
    val pslverr = Output(Bool())

    val uart0_ctrl_en = Output(Bool())
    val uart0_ctrl_loopback = Output(Bool())
    val uart0_status_txEmpty = Input(Bool())
    val uart0_status_rxReady = Input(Bool())
    val uart0_data_txData = Output(UInt(8.W))
    val uart0_data_txData_trg = Output(Bool())
    val uart0_data_rxData = Input(UInt(8.W))
    val uart0_data_rxData_trg = Output(Bool())

    val gpio0_ctrl_en = Output(Bool())
    val gpio0_dir = Output(UInt(32.W))
    val gpio0_dataIn = Input(UInt(32.W))
    val gpio0_dataOut = Output(UInt(32.W))

    val gpio1_ctrl_en = Output(Bool())
    val gpio1_dir = Output(UInt(32.W))
    val gpio1_dataIn = Input(UInt(32.W))
    val gpio1_dataOut = Output(UInt(32.W))
  })

  addPath("soc_adapter.sv")
}


