procedure test(<caret>);
begin
end;

type
    TTest  = class
    public
        function test(param: int; <caret>): int;
    end;

procedure test(const a: Integer; <caret>);
begin
end;

begin
end.
