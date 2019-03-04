unit construct;

interface

type
    TObj = class
        constructor Create();
    end;

    TC= class(TObj)
        y: Integer;
    end;

implementation

{ TObj }

constructor TObj.Create();
begin
end;

begin
    TC.Create().y;
end.