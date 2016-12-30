procedure test1; forward;
procedure test2(); forward;
procedure test3(const param1); forward;
procedure test4(param1, param2: type1); forward;
procedure test5(param1, param2: type1; param3: type2); forward;
function test6: type3; forward;
function test7(): string; forward;
function test8(param4: type5): type6; forward;
function test9(const param5, param6, param7): type7; forward;

function testA<T: typeA0>(arg1: typeA1<T>; const arg2: typeA2): typeA3; forward;

function testB(param1: type1); forward;
function testC; forward;

begin
end.