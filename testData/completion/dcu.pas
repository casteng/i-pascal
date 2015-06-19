// parser stability test
unit dcu;
interface
type
    TTest = class
    var
        spec var
            test.wrong: -$FF..$FF = b.a;
        v: int;
        property test: byte;
        class procedure proc;
    end;
var a: TTest;
implementation
begin
    a.<caret>
end.
