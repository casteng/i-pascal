unit calcTypeTest;

interface

uses types, structTypes;

var
  int: Integer;
  arr: TArray;
  arrP: TArrayP;
  arrPP: PArrayP;
  vec: TVec;
  arrArr: array of TArray;
  clazz: CA;
  arrInt: array of Integer;

implementation

begin
  arr[0][0]^[0].create();
    //arr[0]^[0];
end.