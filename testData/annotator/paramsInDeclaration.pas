unit paramsInDeclaration;

interface

type
    TTest = class
        f: Integer;
        procedure test1();
        <error descr="Missing implementation">procedure test2_invalid();</error>
        <error descr="Missing implementation">procedure test3_invalid(a3: Integer); overload;</error>
        function test4(a4: Integer; b4: string): TTest;
        <error descr="Missing implementation">procedure test5_invalid(a55: string);</error>
        procedure test6(a6: Integer; b6: string); overload;
        procedure test7;
    end;

    function testRoutine1(a: string): TTest;
    <error descr="Missing implementation">function testRoutine2_invalid(a: string): Integer; overload;</error>
    <error descr="Missing implementation">function testRoutine3_invalid(): Integer;</error>

implementation

{ TTest }

procedure TTest.test1();
begin
end;

procedure <error descr="Missing method declaration">TTest.test2_invalid</error>(a2: Integer);
begin
end;

procedure <error descr="Missing method declaration">TTest.test3_invalid</error>();
begin
    <error descr="Undeclared identifier">a3</error> := 1;
end;

function TTest.test4;
begin
    a4 := 4;
    Result.f;
end;

procedure <error descr="Missing method declaration">TTest.test5_invalid</error>(a5: Integer);
begin
end;

procedure TTest.test6(a6: Integer; b6: string);
begin
    a6 := 6;
end;

procedure TTest.test7();
begin
end;

function testRoutine1();
begin
    a;
    Result.f := 7;
end;

function testRoutine2_invalid: Integer;
begin
end;

procedure testRoutine3_invalid(a3: Integer);
begin
end;

end.
