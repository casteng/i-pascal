type
    TParent = class
        constructor ParentConstructor();
        procedure parentMethod();
    end;
    TTest = class(TParent)
        constructor ChildConstructor();
        function ChildMethod(): TParent;
    end;
var
    Test: TTest;

begin
    Test.<caret>
end.
