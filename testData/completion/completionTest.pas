unit completionTest;
interface
type
  Integer = Integer;

  TRec2  = record
    r1: Integer;
  end;
  TRec = record
    rArr: array of TRec2;
  end;
  TArr  = array[0..1] of TRec;

var
  a, b: Int;
threadvar
  arr: TArr;

const
    c = 1;
resourcestring
    rc = 'aa';

implementation

type a=a;

begin
  arr[0].rArr[0].<caret>;
end.
