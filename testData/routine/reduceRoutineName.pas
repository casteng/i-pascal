type
  Type1 = record
  end;
  Type2 = 1..2;

  TAlias1 = Type1;
  TAlias2 = Type2;

  TAlias1Alias = TAlias1;

procedure test3(const param1); forward;
procedure test4(param1, param2: Type1); forward;
procedure test5(param1, param2: Type1; param3: Type2); forward;
function test6: Type1; forward;
function test7(): string; forward;
function test8(param4: TAlias1): TAlias2; forward;
function test9(param1: TAlias1Alias); forward;
function testA<T: TAlias1>(arg1: TAlias1<T>; const arg2: TAlias2): TAlias1; forward;

function testB(param1: TAlias2); forward;

begin
end.