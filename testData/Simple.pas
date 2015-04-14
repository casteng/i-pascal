unit Simple;

interface

uses
    SysUtils, StrUtils, unitTest1,
    //aaa
    objpas, myclass;

type
    int32 = int32;
    TRecord = record
        Name2, CurrentValue, DefaultValue: AnsiString;
        HashValue: LongWord;
        {class function a: int;}
        constructor Create(a, b: int32);
    end;

    RPoint = Record
    Case Boolean of
        False : (X,Y,Z : Real);
        True : (R,theta,phi : Real);
    end;

TEnum = (eOne, eTwo, eThree);

    TC = class(TObject)
    private
        name: string;
    end;

    TC2 = class
    private
        name2: string;
    public
        constructor Create();
        procedure NonStat(a, b: int);
        class procedure Stat();
    end;
    CC2 = class of TTest;

    TStringSearchOption = (soDown, soMatchCase, soWholeWord);
    TStringSearchOptions = set of TStringSearchOption;
    TStringSeachOption = TStringSearchOption;

    Function LeftStr(const AText: AnsiString; const ACount: Integer): AnsiString; inline;
    Function RightStr(const AText: AnsiString; const ACount: Integer): AnsiString; register;
    Function MidStr(const AText: AnsiString; const AStart, ACount: Integer): AnsiString; inline;

implementation

function GetRec: TRecord;
begin

end;

Function LeftStr(const AText: AnsiString; const ACount: Integer): AnsiString; register; overload;
var
    Value: array[0..1] of TTest;
    V2: TC2;
    Obj: TObject;
    a: TLiteInterfacedObject;
    rp: RPoint;

    function nested1(np1: int32): RPoint;
        {function nested2;
        begin
        end;}
    begin
        np1;
        result.X := 1;
    end;

begin
    out := 2+2*3+5-5/2;
    myclass.objpas;
    GetRec.Name2;
    Obj^[0]^^[1].Create(1, 2);
    Value[0].MyProp.Create();
    Value[0].r[0].Name2;
    (Obj as TTest);
    Value.Create();
    v2.Create();
    v2.name2;
    a._AddRef;
    if ((Value mod 2)=1) then
        raise Exception.Create('MyProp can only contain even value');
    FMyInt := Value;
end;

begin
end.
