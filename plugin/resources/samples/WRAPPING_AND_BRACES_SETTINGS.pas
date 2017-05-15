type
    TEnum = (EnumeratedValue1, EnumeratedValue2, EnumeratedValue3, EnumeratedValue4, EnumeratedValue5, EnumeratedValue6);
    TSample<T1, T2> = class(TObject)
    private
        FA: Integer;
    public
        procedure Method(arg1, arg2: Integer; arg3, arg4, arg5: TEnum; b1: TSample);
        function Simple(a1: TEnum): TSample;
        function Modifiers(argument1, argument2: Integer): TEnum; virtual; abstract; register; deprecated;
    end;

// very long comment very long comment very long comment very long comment very long comment
procedure TSample<T1, T2>.Method(arg1, arg2: Integer; arg3, arg4, arg5: TEnum; b1: TSample);
var
    t1, t2: Integer;
begin
    t1 := b1; t2 := arg2;
    if t1 = t2 then
        t1 := t2 else t2 : t1;

    t2 := arg1 + arg2 + arg3 + arg4 * arg1 div arg2 mod arg1;

    if -arg1 > arg2 then
    begin
        FA := arg1 - arg2;
    end else begin FA := (arg1 + -arg2); end;
    Method(arg1 + arg2, arg2, arg3, arg4, arg5, b1);
    Simple(arg3).Simple(arg4).Simple(arg5).Simple(arg3).Simple(arg4).Simple(arg5).Simple(arg4);
end;

function TSample.Simple(a1: TEnum): TSample; begin a1[0] := 0; end;

begin
end.