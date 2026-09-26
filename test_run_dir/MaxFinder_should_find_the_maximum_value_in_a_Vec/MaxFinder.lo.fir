FIRRTL version 1.2.0
circuit MaxFinder :
  module MaxFinder : @[src/main/scala/MaxFinder.scala 9:7]
    input clock : Clock @[src/main/scala/MaxFinder.scala 9:7]
    input reset : UInt<1> @[src/main/scala/MaxFinder.scala 9:7]
    input io_in_0 : UInt<8> @[src/main/scala/MaxFinder.scala 10:14]
    input io_in_1 : UInt<8> @[src/main/scala/MaxFinder.scala 10:14]
    input io_in_2 : UInt<8> @[src/main/scala/MaxFinder.scala 10:14]
    input io_in_3 : UInt<8> @[src/main/scala/MaxFinder.scala 10:14]
    output io_max : UInt<8> @[src/main/scala/MaxFinder.scala 10:14]

    node _io_max_T = gt(io_in_0, io_in_1) @[src/main/scala/MaxFinder.scala 15:46]
    node _io_max_T_1 = mux(_io_max_T, io_in_0, io_in_1) @[src/main/scala/MaxFinder.scala 15:43]
    node _io_max_T_2 = gt(io_in_2, io_in_3) @[src/main/scala/MaxFinder.scala 15:46]
    node _io_max_T_3 = mux(_io_max_T_2, io_in_2, io_in_3) @[src/main/scala/MaxFinder.scala 15:43]
    node _io_max_T_4 = gt(_io_max_T_1, _io_max_T_3) @[src/main/scala/MaxFinder.scala 15:46]
    node _io_max_T_5 = mux(_io_max_T_4, _io_max_T_1, _io_max_T_3) @[src/main/scala/MaxFinder.scala 15:43]
    io_max <= _io_max_T_5 @[src/main/scala/MaxFinder.scala 15:10]
