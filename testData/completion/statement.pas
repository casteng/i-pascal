type Integer = Integer;
var
    a, b: Integer;
    s1: set of Integer;
const c = 1;
begin
    <caret>
    a := b div a;
    s1 := [1, 2..5];
end.