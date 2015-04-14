unit calcTypeTest;

interface

uses types, structTypes;

type
  TInnerRec = record
    innerField: Integer;
  end;
  TOuterRec = record
    arrInRec: array of TInnerRec;
  end;
  TArr = array[0..1] of TOuterRec;
var
  int: Integer;
  arr: TArray;
  arrP: TArrayP;
  arrPP: PArrayP;
  vec: TVec;
  arrArr: array of TArray;
  clazz: CA;
  arrInt: array of Integer;
  ArrVRec: array of TVarRec;
  arr1: TArr;

implementation

begin
  //arr[0][0]^[0].create();
    //arr[0]^[0];
  arr1^.arrInRec[0].innerField;
  //(TOuterRec).arrInRec;
  (int as TOuterRec).arrInRec;
end.