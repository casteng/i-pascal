unit Sample;
interface
uses SysUtils;
type
    TArr = array[0..100] of Integer;
    TRec = record a: TArr;b: Integer;end;


    TAlias = TArr;
    TSample<T1, T2> = class(TObject)
    private
        FA: Integer;FB: Integer;
    public
        function Method(a1, a2: Integer; const b1: TSample): TSample;
        property A: read FA write FA; default;
    end;
procedure routine();
implementation
uses unix;
function TSample<T1, T2>.Method(a1, a2: Integer; const b1: TSample): TSample;
var
    t1, t2: TArr;
    t3: Integer;
begin
    t1[0] := a1;t2[1] := a2;
    Result := b1;
    if -a1 > a2 then
    begin
        FA := a1 - a2;
    end else begin
        FA := (a1 + -a2);
    end;

    try
        case t3 of
        1: t1 := t2;
        2: t3 := t1;
        else t2 := t3 end;
    except on
    E: Exception do
        CELog.Error('Error occured: ' + E.Message);end;


    Method(a1, a2, b1);


end;
procedure routine();
begin
end;
end.