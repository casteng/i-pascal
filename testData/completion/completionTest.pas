type
  Integer = Integer;
  TRec2 = record
    r1: Integer;
  end;
  TRec = record
    rArr: array of TRec2;
  end;
  TArr = array[0..1] of TRec;
var
  a, b: Int;
  arr: TArr;
const c = 1;
begin
  arr[0].rArr[0].<caret>;
end.