
import os
import sys
import pandas as pd

# read first arg
file_path = sys.argv[1]

# strip file extension
base_name = os.path.splitext(os.path.basename(file_path))[0]

output_file = base_name + "_adapter.sv"

# Read a single sheet
df = pd.read_excel(file_path, sheet_name=None)


io = ""
regs = ""
outAssigns = ""
writeAccess = ""
readAccess = ""
resetStmts = ""
writeTrg = ""
readTrg = ""
readableAddr = []
writeableAddr = []

busWriteData = "pwdata"
busReadData = "prdata"
busWriteAddr = "paddr"

def toVerilogLit(value, width):
    if isinstance(value, int):
        return f"{width}'h{value:X}"
    elif isinstance(value, str) and value.startswith("0x"):
        return f"{width}'h{int(value, 16):X}"
    else:
        raise ValueError(f"Unsupported value type: {type(value)} for value {value}")

def case_stmt(case, lines):
   if len(lines) == 1:
       return f"{case}: {lines[0]}"
   else:
       joined = "".join(lines)
       return f"{case}: begin\n{indent(joined, 1)}\nend\n"
   
def add_addr(typ, addr):
    global readableAddr, writeableAddr
    addrStr = toVerilogLit(addr, 32)
    if typ == "rw":
        readableAddr.append(addrStr)
        writeableAddr.append(addrStr)
    elif typ == "ro":
        readableAddr.append(addrStr)
    elif typ == "wo":
        writeableAddr.append(addrStr)
    elif typ == "wotrg":
        writeableAddr.append(addrStr)
    elif typ == "rotrg":
        readableAddr.append(addrStr)
    elif typ == "const":
        readableAddr.append(addrStr)

def indent(str, lvl):
   # indent each line
   return "\n".join([f"{'  ' * lvl}{line}" for line in str.splitlines()])

def add_reset_statement(name, width, init):
    global resetStmts
    initStr = toVerilogLit(init, width)
    resetStmts += f"{name}_reg <= {initStr};\n"

def add_reset(typ, name, width, init):
   if init == "?":
       return
   if typ == "rw":
       add_reset_statement(name, width, init)
   elif typ == "wotrg":
       add_reset_statement(name, width, init)

def read_access(name, range):
    return f"{busReadData}[{range}] = {name}_reg;\n"

def create_write_trg(typ, name, address):
    global writeTrg
    if typ == "wotrg":
        writeTrg += f"assign {name}_trg = wr_access && (paddr == {address});\n"
    
def create_read_trg(typ, name, address):
    global readTrg
    if typ == "rotrg":
        readTrg += f"assign {name}_trg = rd_access && (paddr == {address});\n"

def create_read(typ, name, range, const, width):
    if typ == "rw":
        return [read_access(name, range)]
    elif typ == "rotrg":
        return [f"{busReadData}[{range}] = {name};\n"]
    elif typ == "ro":
        return [f"{busReadData}[{range}] = {name};\n"]
    elif typ == "const":
        return [f"{busReadData}[{range}] = {toVerilogLit(init, width)}; // {name}\n"]
    else:
        return []

def write_access(name, range):
    return f"{name}_reg <= {busWriteData}[{range}];\n"

def create_write(typ, name, range):
    if typ == "rw":
        return [write_access(name, range)]
    elif typ == "wotrg":
        return [write_access(name, range)]
    else:
        return []

def add_reg_statement(name, width):
    global regs
    if width == 1:
        regs += f"reg {name}_reg;\n"
    else:
        regs += f"reg [{width - 1}:0] {name}_reg;\n"

def add_reg(typ, name, width):
    if typ == "rw":
        add_reg_statement(name, width)
    elif typ == "wotrg":
        add_reg_statement(name, width)

def add_io_statement(dir, name, width):
    global io
    if width == 1:
        io += f"{dir} logic {name},\n"
    else:
        io += f"{dir} logic [{width - 1}:0] {name},\n"

def add_io(typ, name, width):
    if typ == "rw":
        add_io_statement("output", name, width)
    elif typ == "ro":
        add_io_statement("input", name, width)
    elif typ == "wotrg":
        add_io_statement("output", name, width)
        add_io_statement("output", f"{name}_trg", 1)
    elif typ == "rotrg":
        add_io_statement("input", name, width)
        add_io_statement("output", f"{name}_trg", 1)

def add_out_assign(typ, name, width):
    global outAssigns
    if typ == "rw":
        outAssigns += f"assign {name} = {name}_reg;\n"
    elif typ == "wotrg":
        outAssigns += f"assign {name} = {name}_reg;\n"

for block in df['Map']['Name']:
  info = df['Map'][df['Map']['Name'] == block].values[0]
  base_addr = int(info[3], 16)
  end_addr = int(info[4], 16)
  block_type = info[0]
  print(f"{block}: {block_type} [{hex(base_addr)} - {hex(end_addr)}]")
  for reg in df[block_type]['Register'].unique():
    if pd.notna(reg):
      print(f"  - {reg}", end="")
      reg_info = df[block_type][df[block_type]['Register'] == reg].values[0]
      offset = int(reg_info[1], 16)
      writes =[]
      write_resps = []
      reads = []
      read_trgs = []
      write_trgs = []
      
      if pd.isna(df[block_type][df[block_type]['Register'] == reg]['Field'].values[0]):
        print(f"")
        typ = reg_info[3]
        rang = reg_info[4]
        # parse X:X from range
        upr, lwr = map(int, rang.split(":"))
        width = upr - lwr + 1
        init = reg_info[5]

        add_io(typ, f"{block}_{reg}", width)
        add_reg(typ, f"{block}_{reg}", width)
        writes += create_write(typ, f"{block}_{reg}", rang)
        reads += create_read(typ, f"{block}_{reg}", rang, init, width)
        create_write_trg(typ, f"{block}_{reg}", toVerilogLit(base_addr + offset, 32))
        create_read_trg(typ, f"{block}_{reg}", toVerilogLit(base_addr + offset, 32))
        add_reset(typ, f"{block}_{reg}", width, init)
        add_addr(typ, base_addr + offset)
        add_out_assign(typ, f"{block}_{reg}", width)

      else:
        print(" { ", end="")
        for field in df[block_type][df[block_type]['Register'] == reg]['Field']:
          if pd.notna(field):
            field_info = df[block_type][df[block_type]['Field'] == field].values[0]
            typ = field_info[3]
            rang = field_info[4]
            # parse X:X from range
            upr, lwr = map(int, rang.split(":"))
            width = upr - lwr + 1
            init = field_info[5]
            print(f"{field}[{rang}]; ", end="")

            add_io(typ, f"{block}_{reg}_{field}", width)
            add_reg(typ, f"{block}_{reg}_{field}", width)
            writes += create_write(typ, f"{block}_{reg}_{field}", rang)
            reads += create_read(typ, f"{block}_{reg}_{field}", rang, init, width)
            create_write_trg(typ, f"{block}_{reg}_{field}", toVerilogLit(base_addr + offset, 32))
            create_read_trg(typ, f"{block}_{reg}_{field}", toVerilogLit(base_addr + offset, 32))
            add_reset(typ, f"{block}_{reg}_{field}", width, init)
            add_addr(typ, base_addr + offset)
            add_out_assign(typ, f"{block}_{reg}_{field}", width)
        print("}")
    if writes:
        writeAccess += case_stmt(f"{toVerilogLit(base_addr + offset, 32)}", writes)
    if reads:
        readAccess += case_stmt(f"{toVerilogLit(base_addr + offset, 32)}", reads)
  print("")


# make addr unique
readableAddr = sorted(set(readableAddr))
writeableAddr = sorted(set(writeableAddr))



module = f"""
module {base_name}_adapter (
  input logic clock,
  input logic reset,

  // apb
  input logic psel,
  input logic penable,
  input logic [31:0] paddr,
  input logic pwrite,
  input logic [31:0] pwdata,
  output logic [31:0] prdata,
  output logic pready,
  output logic pslverr,

{indent(io[:-2], 1)}
);

  // Apb state
  reg wr_access;
  reg rd_access;
  assign pready = wr_access || rd_access;

  // Internal registers
{indent(regs,1)}

  // Output assignments
{indent(outAssigns,1)}

  // Write triggers
{indent(writeTrg,1)}

  // Read triggers
{indent(readTrg,1)}

  always_ff @(posedge clock) begin // APB phases
    if (reset) begin
      wr_access <= 1'b0;
      rd_access <= 1'b0;
    end else begin
      wr_access <= wr_access ? 1'b0 : psel && pwrite;
      rd_access <= rd_access ? 1'b0 : psel && !pwrite;
    end
  end

  always_ff @(posedge clock) begin // Register reset and writes
    if (reset) begin
{indent(resetStmts, 3)}
    end else begin
      if (wr_access) begin
        case (paddr)
{indent(writeAccess,5)}
        endcase
      end
    end
  end

  always_comb begin // APB error handling
    if (psel) begin
      if (rd_access) begin
        case (paddr)
          {", ".join(readableAddr)}: pslverr = 1'b0;
          default: pslverr = 1'b1;
        endcase
      end else if (wr_access) begin
        case (paddr)
          {", ".join(writeableAddr)}: pslverr = 1'b0;
          default: pslverr = 1'b1;
        endcase
      end
    end
  end

  always_comb begin // Read Access
    prdata = 32'h00000000;
    if (psel) begin
      case (paddr)
{indent(readAccess, 4)}
      endcase
    end
  end

  

endmodule
"""

# write to file
with open(output_file, "w") as f:
    f.write(module)