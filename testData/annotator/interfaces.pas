unit interfaces;

interface

type
    CustomAttribute = CustomAttribute;
    CustomAttribute2 = CustomAttribute2;
    Integer = Integer;
    Single = Single;

    [CustomAttribute('', '', False)]
    IUnknown = interface
    ['{00000000-0000-0000-C000-000000000046}']
        function func(): [CustomAttribute('', '', False)] Single; virtual;
        procedure proc; virtual;
    end;

    IEnum<T> = interface
        [CustomAttribute('virtual T __fastcall GetCurrentT(void) = 0')]
        function GetCurrent: T;
        [CustomAttribute2('__property T Current = {read=GetCurrentT}')]
        property Current: T read GetCurrent;
    end;


    [CustomAttribute, CustomAttribute2('', '', False)]
    TA = class
    private
        [CustomAttribute('', '', False)]
        procedure IntfGetComponent; virtual;
    public
        function IUnknown.proc = IntfGetComponent;
    end;

implementation

procedure TA.IntfGetComponent;
begin

end;


end.