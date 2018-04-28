unit variables;

interface

type
    Integer = Integer;
    Single = Single;

    TRec = record
        v1: Integer;
    end;

    TCls = class
    public
        procedure Test(Rec: TRec);
    end;

implementation

var
    func: function(TotalFree: Integer): Integer; stdcall = nil;
    proc: procedure(TotalFree: Integer); stdcall = nil;
    a: Single<procedure, function: a>;


procedure TCls.Test(Rec: TRec);
var v: Integer absolute Rec.v1;
begin

end;

end.