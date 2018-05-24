type Integer = Integer;
var
    a, b: Integer;
    s1: set of Integer;
begin
    a := b div a;
    for a := 0 to 1 <caret> ;
    if a=b <caret>;
    while a=b <caret>;
    case a <caret>
    end;
    try
    except
        on E : TException <caret>
    end;
    s1 := [1, 2..5];
end.