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

  CA = class of TA;

  TAHelper = class helper for TA
    Name: string;
    class function Func: TA;
    constructor Create(a, b: TA);
  end;

  PVec = ^TVec;
  TVec = packed record
    x, Y: Single;
    z: TA;
  end;

  TVecHelper = record helper for TVec
    class function v: TVec;
    constructor Create();
  end;

  TVarRec = packed record
    X, Y: Single;
  case Single of
    0: (X, Y: Single)
    1: (V: array[0..1] of Single)
  end;

  TVarRec2 = record
  case a: Single of
    0: (X, Y: Single)
  end;

  TArray = array[0..100] of TA;
  TArrayP = array of PVec;
  PArrayP = ^TArrayP;

implementation

var
  vec: TVec;
a: tvarrec;
begin
  with a do begin
    v[0] := 1;
  end;
  with vec do
    Y := 1;
end.