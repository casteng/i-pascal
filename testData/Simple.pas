unit Simple;

interface

uses
    classes, unitTest1,
    //aaa
    objpas, scoped.myclass;

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

    TC2 = class(system.TObject)
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
    //Function LeftStr(const AText: AnsiString; const ACount: Integer): AnsiString; register;
    Function LeftStr(const AText: AnsiString; const AStart, ACount: Integer): AnsiString; inline;
    Function LeftStr: a;

    procedure fpc_AddRef(Data, TypeInfo: Pointer); external name 'FPC_ADDREF';

implementation

function GetRec: TRecord;
var cc: TComponent;
begin
    leftstr();
    cc.Free();
end;

Function LeftStr(const AText: AnsiString; const ACount: Integer): AnsiString;
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
        result := 1;
    end;

begin
    out := 2+2*3+5-5/2;
    GetRec.Name2;
    Obj^[0]^^[1].Create(1, 2);
    Value[0].MyProp.Create();
    Value[0].r[0].Name2;
    (Obj as TTest);
    Value.Create();
    v2.Create();
    v2.name2;
    v2.Stat();
    a._AddRef;
    if ((Value mod 2)=1) then
        raise Exception.Create('MyProp can only contain even value');
    FMyInt := Value;
end;

begin

end.
