unit structIntf;
interface
type
    TTest  = class
    public
        function test(param: int): int;
        <caret>
    end;

implementation

end.
