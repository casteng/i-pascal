unit resolveInherited;

interface

type
    TParent = class
        function test(): TParent;
    end;

    TChild = class(TParent)
        function test(): TParent;
    end;

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

end.
