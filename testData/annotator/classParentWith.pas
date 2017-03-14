unit classParentWith;

interface

type
    TParent = class
        procedure test1(); virtual; abstract;
    end;

    TChild= class(TParent)
    end;

implementation

var
    Child: TChild;


begin
    with Child do
        test1();
end.