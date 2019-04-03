unit overload;

interface

type
    TR = record
        x: Integer;
    end;
    TC= class
        y: Integer;
    end;

    function GetR(): TR; external;
    function GetR(a: Integer): TC; external;

implementation

begin
    GetR().x;
    GetR(1).y;
    GetR.x;
end.