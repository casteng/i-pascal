unit infereTypePath;

interface

type
  TInnerRec = record
    innerField: Integer;
  end;
  TOuterRec = record
    arrInRec: array of TInnerRec;
  end;
  TArr = array[0..1] of TOuterRec;
  TClass = class of TObject;
  PArr = ^TArr;

type
  TEnum = (V1, V2);
  PEnum = ^TEnum;

var
  arrArr: array of TArr;
  arr1: TArr;
  arrPtr: PArr;
  ArrVRec: array of TVarRec;
  rec: TOuterRec;
  ptr: PEnum;

implementation

begin
  rec.arrInRec;
  rec.arrInRec[0].innerField;
  arr1[0];
  rec.arrInRec[0];
  ptr^;

  obj[0].A;
  arrPtr^[0].arrInRec[0].innerField;
  (TOuterRec).arrInRec;
  (int as TOuterRec).arrInRec;
  PropInfo^.PropType^.Kind
end.