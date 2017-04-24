unit forwardStructure;

interface

type
    IFoo = interface;
    TFoo = class;

    IFoo = interface
        function Bar(): Boolean;
    end;

    TFoo = class
    public
        Test: IFoo;
    end;

implementation

procedure test();
var
    f: TFoo;
begin
    f.Test.<caret>
end;

end.