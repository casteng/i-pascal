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
  AB : array[0..1] of TB;
  R: record
    y: integer;
  end;
  AAA : array of array of TA;
  AOR : array[0..1] of record
    x: Integer;
  end;

property PropA: TA;

procedure proc(); external;

function func(): TR; external;

begin

end.
