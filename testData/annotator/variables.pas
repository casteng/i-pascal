unit structTypes;

interface

type
    Integer = Integer;
    Single = Single;

    TRec = record
        v1: Integer;
    end;

    TCls = class()
    public
        procedure Test(Rec: TRec);
    end;

implementation

procedure TCls.Test(Rec: TRec);
var v: Integer absolute Rec.v1;
begin

end;

end.