unit infereTypePath;

interface

type
  TRec = class
    Field: Integer;
  end;

var
  Rec: TRec;
  Obj: TObject;
  Ptr: ^TRec;
  b: Byte;
  w: Word;

implementation

var
  i: Integer;

function test(): TRec;
begin
end;

begin
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
end.