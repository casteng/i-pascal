unit unimplementedMethods;
interface

type
    TA = TA;
    IParent = interface
        function implemented(A1: TA): IParent;
        function notImplemented(A1, A2: TA): IParent;
    end;

    TTest = class<warning descr="W0005: Class does not implement method IParent.notImplemented(A1:TA,A2:TA):IParent">(IParent)</warning>
        function implemented(A1: TA): IParent;
    end;

implementation

function TTest.implemented(A1: TA): IParent;
begin
end;

end.
