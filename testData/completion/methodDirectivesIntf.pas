unit methodIntf;
interface
type
    TTest  = class
    public
        function test(param: int): int; dispid; <caret>
    end;

implementation

end.
