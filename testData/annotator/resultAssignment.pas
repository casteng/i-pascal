unit resultAssignment;
interface

type
  int=int;

implementation

procedure modify(var con: int);
begin
end;

function testOK1(): int;
begin
  Result := 0;
  exit;
end;

function testOK2(): int;
begin
  exit(1);
end;

function testOK3(): int;
begin
  modify(Result);
  exit;
end;

function testWarn1(): int;
var a: int;
begin
    a := 0;
<warning descr="W0007: No result assignment in a function">end</warning>;

function testWarn2(): int;
var a: int;
begin
  a := 0;
  <warning descr="W0007: No result assignment in a function">exit</warning>;
  Result := 1;
  modify(Result);
end;

end.
