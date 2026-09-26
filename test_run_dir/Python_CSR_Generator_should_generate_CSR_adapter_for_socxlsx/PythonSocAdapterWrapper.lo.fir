FIRRTL version 1.2.0
circuit PythonSocAdapterWrapper :
  extmodule soc_adapter : @[src/test/scala/PythonSoCAdapter.scala 67:7]
    input clock : Clock
    input reset : UInt<1>
    input psel : UInt<1>
    input penable : UInt<1>
    input paddr : UInt<32>
    input pwrite : UInt<1>
    input pwdata : UInt<32>
    output prdata : UInt<32>
    output pready : UInt<1>
    output pslverr : UInt<1>
    output uart0_ctrl_en : UInt<1>
    output uart0_ctrl_loopback : UInt<1>
    input uart0_status_txEmpty : UInt<1>
    input uart0_status_rxReady : UInt<1>
    output uart0_data_txData : UInt<8>
    output uart0_data_txData_trg : UInt<1>
    input uart0_data_rxData : UInt<8>
    output uart0_data_rxData_trg : UInt<1>
    output gpio0_ctrl_en : UInt<1>
    output gpio0_dir : UInt<32>
    input gpio0_dataIn : UInt<32>
    output gpio0_dataOut : UInt<32>
    output gpio1_ctrl_en : UInt<1>
    output gpio1_dir : UInt<32>
    input gpio1_dataIn : UInt<32>
    output gpio1_dataOut : UInt<32>
    defname = soc_adapter

  module PythonSocAdapterWrapper : @[src/test/scala/PythonSoCAdapter.scala 6:7]
    input clock : Clock @[src/test/scala/PythonSoCAdapter.scala 6:7]
    input reset : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 6:7]
    input io_psel : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_penable : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_paddr : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_pwrite : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_pwdata : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_prdata : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_pready : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_pslverr : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_uart0_ctrl_en : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_uart0_ctrl_loopback : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_uart0_status_txEmpty : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_uart0_status_rxReady : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_uart0_data_txData : UInt<8> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_uart0_data_txData_trg : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_uart0_data_rxData : UInt<8> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_uart0_data_rxData_trg : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_gpio0_ctrl_en : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_gpio0_dir : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_gpio0_dataIn : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_gpio0_dataOut : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_gpio1_ctrl_en : UInt<1> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_gpio1_dir : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    input io_gpio1_dataIn : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]
    output io_gpio1_dataOut : UInt<32> @[src/test/scala/PythonSoCAdapter.scala 7:14]

    inst adapter of soc_adapter @[src/test/scala/PythonSoCAdapter.scala 36:23]
    io_prdata <= adapter.prdata @[src/test/scala/PythonSoCAdapter.scala 44:13]
    io_pready <= adapter.pready @[src/test/scala/PythonSoCAdapter.scala 45:13]
    io_pslverr <= adapter.pslverr @[src/test/scala/PythonSoCAdapter.scala 46:14]
    io_uart0_ctrl_en <= adapter.uart0_ctrl_en @[src/test/scala/PythonSoCAdapter.scala 47:20]
    io_uart0_ctrl_loopback <= adapter.uart0_ctrl_loopback @[src/test/scala/PythonSoCAdapter.scala 48:26]
    io_uart0_data_txData <= adapter.uart0_data_txData @[src/test/scala/PythonSoCAdapter.scala 51:24]
    io_uart0_data_txData_trg <= adapter.uart0_data_txData_trg @[src/test/scala/PythonSoCAdapter.scala 52:28]
    io_uart0_data_rxData_trg <= adapter.uart0_data_rxData_trg @[src/test/scala/PythonSoCAdapter.scala 54:28]
    io_gpio0_ctrl_en <= adapter.gpio0_ctrl_en @[src/test/scala/PythonSoCAdapter.scala 55:20]
    io_gpio0_dir <= adapter.gpio0_dir @[src/test/scala/PythonSoCAdapter.scala 56:16]
    io_gpio0_dataOut <= adapter.gpio0_dataOut @[src/test/scala/PythonSoCAdapter.scala 58:20]
    io_gpio1_ctrl_en <= adapter.gpio1_ctrl_en @[src/test/scala/PythonSoCAdapter.scala 59:20]
    io_gpio1_dir <= adapter.gpio1_dir @[src/test/scala/PythonSoCAdapter.scala 60:16]
    io_gpio1_dataOut <= adapter.gpio1_dataOut @[src/test/scala/PythonSoCAdapter.scala 62:20]
    adapter.clock <= clock @[src/test/scala/PythonSoCAdapter.scala 37:20]
    adapter.reset <= reset @[src/test/scala/PythonSoCAdapter.scala 38:20]
    adapter.psel <= io_psel @[src/test/scala/PythonSoCAdapter.scala 39:19]
    adapter.penable <= io_penable @[src/test/scala/PythonSoCAdapter.scala 40:22]
    adapter.paddr <= io_paddr @[src/test/scala/PythonSoCAdapter.scala 41:20]
    adapter.pwrite <= io_pwrite @[src/test/scala/PythonSoCAdapter.scala 42:21]
    adapter.pwdata <= io_pwdata @[src/test/scala/PythonSoCAdapter.scala 43:21]
    adapter.uart0_status_txEmpty <= io_uart0_status_txEmpty @[src/test/scala/PythonSoCAdapter.scala 49:35]
    adapter.uart0_status_rxReady <= io_uart0_status_rxReady @[src/test/scala/PythonSoCAdapter.scala 50:35]
    adapter.uart0_data_rxData <= io_uart0_data_rxData @[src/test/scala/PythonSoCAdapter.scala 53:32]
    adapter.gpio0_dataIn <= io_gpio0_dataIn @[src/test/scala/PythonSoCAdapter.scala 57:27]
    adapter.gpio1_dataIn <= io_gpio1_dataIn @[src/test/scala/PythonSoCAdapter.scala 61:27]
