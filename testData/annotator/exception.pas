unit exception;

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
    except
        on E: TException do
        begin
            print(E.field);
        end;
        on TException do
            print(nil);
    end;

    try
    except
        print(nil);
    end;
end.
