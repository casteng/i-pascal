unit classes;

interface

type
    Integer = Integer;
    TA = class()
    private
    type
        TInner = class
            f: Integer;
            procedure m();
        end;
    protected
    public
        class destructor Destroy();
    published
    end;

implementation

procedure TA.TInner.m;
begin
end;

class destructor TA.Destroy();
begin
end;

function TA.m2(const V: TVarData; const Operator: TVarOp; out RequiredVarType: TVarType): Boolean; forward;

end.