unit nestedRoutines;

interface

    function outer(): Integer;

implementation

type
    TRec = record
        X, Y: Integer;
    end;

function outer(): Integer;
    function nested(): TRec;
    begin
        Result.X;
    end;

begin
end;


end.