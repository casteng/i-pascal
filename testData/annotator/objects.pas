unit objects;

interface

type
    Integer = Integer;
    Single = Single;

    TParent = object
        i: Integer;
    end;

    TChild = object(TParent)
    end;

implementation

var
    child: TChild;
begin
    child.i;
end.