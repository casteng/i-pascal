unit Sample;
interface
uses SysUtils;
type
    TArr = array[0..100] of Integer;


    TAlias = TArr;
    TSample<T1, T2> = class(TObject)
    private
        FA: Integer;
    public
        function Method(a1, a2: Integer; b1: TSample): TSample;
        property A: read FA write FA;
    end;
procedure routine();

implementation

function TSample<T1, T2>.Method(a1, a2: Integer; b1: TSample): TSample;
var
    t1, t2: Integer;
begin
    Result := b1;
    if -a1 > a2 then
    begin
        FA := a1 - a2;
    end else begin
        FA := (a1 + -a2);
    end;

    Method(a1, a2, b1);

end;
procedure routine();
begin
end;
end.