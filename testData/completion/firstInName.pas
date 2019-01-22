unit firstInName;
interface
type
  Integer = Integer;

  TRec  = record
    r1: Integer;
  end;

var
  a, b: TRec;

implementation

begin
  a.<caret>
  b;
end.
