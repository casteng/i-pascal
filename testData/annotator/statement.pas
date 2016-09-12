type
    TInt = TInt;
var
    test: TInt;
begin
    case test of
        15, 16: test := 0;
        24:     test := 1;
        32:     test := 2;
    end;
end.
