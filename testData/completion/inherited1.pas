uses inherited2;

type
    TParent = class(TParent2)
        constructor ParentConstructor();
        procedure parentMethod();
    strict private
        procedure strictPrivateMethod();
    end;
    TTest = class(TParent)
        constructor ChildConstructor();
        function ChildMethod(): TParent;
    end;
var
    Test: TTest;

{ TTest }

function TTest.ChildMethod(): TParent;
begin
    inherited <caret>
end;

begin
end.
