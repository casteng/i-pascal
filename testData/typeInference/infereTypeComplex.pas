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

implementation

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
end.