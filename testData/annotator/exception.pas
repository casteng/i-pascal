unit calcTypeTest;

interface

type
    Integer = Integer;
    TException = class
        field: Integer;
    end;

implementation

function print(E: TException): TException;
begin
end;

begin
    try
    except on E: TException do
    begin
        print(E.field);
    end;
    end;
end.