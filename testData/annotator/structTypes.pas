unit structTypes;

interface

type
    Integer = Integer;
    Single = Single;

    IUnknown = interface
    ['{00000000-0000-0000-C000-000000000046}']
        function func(): Single; virtual;
        procedure proc; virtual;
    end;

    TA = class
        FA: string;
    strict private
        FPrivate: TA;
    private
    strict protected
        FProtected: TA;
    protected
    public
    published
        property A: TA read FPrivate write FPrivate;
    end;

    TObserverMapping = class
    public
      const
        EditLinkID = 1;
        EditGridLinkID = 2;
        PositionLinkID = 3;
        MappedID = 100;
    private
      FMappings: TObserverMapping;
      class var
        FInstance: TObserverMapping;
    protected
      class function Instance: TObserverMapping;
    public
        constructor Create;
        destructor Destroy; override;
        class destructor Destroy;
        class function GetObserverID(const Key: Single): Integer;
        property DefProp[ind: Integer]: TA; default;
    end;

    CA = class of TA;

    PVec = ^TVec;
    TVec = packed record
        x, Y: Single;
        z: TA;
    end;

    TVarRec = packed record
        X, Y: Single;
    case Single of
        0: (X, Y: Single);
        1: (V: array[0..1] of Single)
    end;

    TBitRec = bitpacked record
        OneBit, a: 0..1;
        TwoBits: 0..3;
        FourBits: 0..15;
        EightBits: 0..255;
    end;

    TVarRec1 = record
    case tag: Single of
        1: (a: Integer)
    end;

    TVarRec2 = record
        aa: Integer;
    private
        case aa: Single of
            1: (aa: Integer);
            0: (
                X1, Y1: Single;
                case aa: Single of
                0: (X, Y: Single)
            )
    end;

    TOuter = record
    case Byte of
        1: (inner: record f1: Byte; end);
    end;

    TArray = array[0..100] of TA;
    TArrayP = bitpacked array of PVec;
    PArrayP = ^TArrayP;

    TOSVersion = record
    public type
        TPlatform = (pfWindows, pfMacOS);
    private
        class var FPlatform: TPlatform;
    public
        class property Platform: TPlatform read FPlatform;
    end;

implementation

var
    vec: TVarRec2;
    a: tvarrec;
    o: TOuter;

class function TObserverMapping.Instance: TObserverMapping;
begin

end;

constructor TObserverMapping.Create;
var structTypes: TVec;
begin
    structTypes.x;
end;

class destructor TObserverMapping.Destroy;
begin

end;

class function TObserverMapping.GetObserverID(const Key: Single): Integer;
begin

end;

var
  vr2: TVarRec1;

begin
    with a do begin
        v[0] := 1;
        v[1].X := 0;
    end;
    with TVarRec2(a) do begin
        aa := 111;
    end;
    with a as TVarRec2 do
        aa := 111;
    with vec do
        Y := 1;
    vr2.tag := 1;
    o.inner;
    o.inner.f1;
end.