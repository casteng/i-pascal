unit methodIntf;
interface
type
    TTest  = class
    public
        function test(param: int): int;
    end;

implementation

{ TTest }

function TTest.test(param: int): int;
<caret>
begin

end;

end.
