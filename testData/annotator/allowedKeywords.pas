unit allowedKeywords;

interface

type
    Integer = Integer;
    Single = Single;

    reference = record
        v1: Integer;
    end;

    TCls = class
    public
        procedure Message2(Rec: Integer);
    end;

implementation

procedure TCls.Message2(Rec: Integer);
begin

end;

end.