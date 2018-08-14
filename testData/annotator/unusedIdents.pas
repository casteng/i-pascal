unit unusedIdents;
interface
implementation
type
    <warning descr="Identifier declared but never used">TC</warning> = class
        function testf1(): Integer; override;
    end;

var
    <warning descr="Identifier declared but never used">var1</warning>: Integer;
    <warning descr="Identifier declared but never used">Rec</warning>: record
        yyy: Integer;
    end;

const <warning descr="Identifier declared but never used">CCC</warning>=1;

procedure <warning descr="Identifier declared but never used">a</warning>(const <warning descr="Identifier declared but never used">arg1</warning>: Integer);
var <warning descr="Identifier declared but never used">local</warning>: Integer;
begin
end;

function TC.testf1(): Integer;
begin
end;

begin
end.
