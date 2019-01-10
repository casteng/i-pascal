unit infereTypeCall;

interface

type
  TEnum = (V1, V2);
  PEnum = ^TEnum;

implementation

function test(out enumPtr: PEnum): TEnum;
begin
end;

procedure test2(var enum: TEnum; out enumPtr: PEnum);
begin
end;

begin
  test(a);
  test2(a1 , a2);
end.