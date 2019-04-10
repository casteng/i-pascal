unit routineCalls;

interface

type
    Int = Int;
    TC = class
        procedure method(a: Int);
    end;

    function test1(a: Int): Int;

implementation

function test1(a: Int): Int;
begin
end;

procedure TC.method(a: Int);
begin
end;

var
  callback: Int;
  C: TC;
begin
    callback := test1;
    callback := TC.method;
end.