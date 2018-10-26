unit unusedIdents;
interface
implementation
type
    <warning descr="W0003: Identifier declared but never used">TC</warning> = class
        function <warning descr="W0003: Identifier declared but never used">testf1</warning>(): Integer;
    end;

var
    <warning descr="W0003: Identifier declared but never used">var1</warning>: Integer;
    <warning descr="W0003: Identifier declared but never used">Rec</warning>: record
        yyy: Integer;
    end;

const <warning descr="W0003: Identifier declared but never used">CCC</warning>=1;

procedure <warning descr="W0003: Identifier declared but never used">a</warning>(const <warning descr="W0003: Identifier declared but never used">arg1</warning>: Integer);
var <warning descr="W0003: Identifier declared but never used">local</warning>: Integer;
begin
end;

function TC.testf1(): Integer;
begin
end;

begin
end.
