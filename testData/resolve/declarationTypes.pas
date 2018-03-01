type
  TB = TB;
  TA = TB;
  TC = class of TA;
  TR = record
    x: Integer;
  end;
const
  CA: TA = 1;
var
  A: TA;
  B: TB;
  PB: ^TB;
  AA : array[0..1] of TB;
  R: record
    y: Integer;
  end;

property PropA: TA;

procedure proc(); external;

function func(): TR; external;

begin

end.
