type TEnum = (Value1,Value2,Value3,  Value4);
    TSample<T1, T2> = class(TObject,IInterface)
    private
        FA: Integer;
    public
        procedure Method(arg1, arg2: Integer; arg3: TEnum);
        function Simple(a1: TEnum): TSample;
        function Modifiers(a, b: TEnum): TEnum; virtual; abstract; register;
    end;

// comment
procedure TSample<T1, T2>.Method(arg1, arg2: Integer; arg3: TEnum);
var t1, t2: Integer;
begin
    t1 := arg2; t2 := arg2;
    if t1 = t2 then
        t1 := t2 else t2 := t1;

    t2 := arg1 + arg2 + arg3 * arg1 div arg2 mod arg1;

    if -arg1 > arg2 then begin
        FA := arg1 - arg2;
    end else begin FA := (arg1 + -arg2); end;
    Method(arg1 + arg2, arg2, arg3);
    Simple(arg3).Simple(arg1).Simple(arg3).Simple(arg2);
end;

function TSample.Simple(a1: TEnum): TSample; begin a1[0] := 0; end;

function NotSimple(a1: TEnum): TSample; begin a1[0] := 0; a1 := a1; end;

begin
end.