unit infereTypeComplex;

interface

type
  TRec = class
    Field: Integer;
    constructor Create();
  end;

var
  Rec: TRec;
  Obj: TObject;
  Ptr: ^TRec;
  b: Byte;
  w: Word;

implementation

const
  C = 1;

var
  i: Integer;

function test(): TRec;
begin
end;

begin
  C;
  2-2;
  (1+1);
  2 * 0.1 + 2;
  @Rec;

  1 = 2;
  Obj as SomeType;
  test();
  SomeType(Rec);
  b + w;
  w + i;

  -2147483648;
  -2147483649;
  2147483648;
  -9223372036854775808;
  18446744073709551615;
  b - w;
    
  b / w;
  3.1428;
  2.12345678;
  2.1234567812345678;
  TRec.Create();
end.