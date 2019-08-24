unit unit2;

interface

uses
  unit1;

implementation

var
  P: TParent;
  C: TChild;

begin
  test();
  P.test();
  C.test();
end.
