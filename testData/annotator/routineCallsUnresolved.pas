unit routineCallsUnresolved;

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
    callback := <error descr="Undeclared identifier">test1</error>();
    callback := TC.<error descr="Undeclared identifier">method</error>();
end.