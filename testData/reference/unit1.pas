unit unit1;

interface

type
  TParent = class
    function test(): TParent;
  end;

  TChild = class(TParent)
    function test(): TParent;
  end;

  procedure test();

implementation

{ TParent }

function TParent.test(): TParent;
begin
end;

{ TChild }

function TChild.test(): TParent;
begin
  inherited;
end;

procedure test();
begin
end;

var
  P: TParent;
  C: TChild;

begin
  test();
  P.test();
  C.test();
end.
