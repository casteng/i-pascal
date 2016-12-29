unit sectionToggle;

interface

type
    TTest = class
        procedure test1();
        procedure test2_invalid();
        procedure test3_invalid(a3: Integer); overload;
        procedure test4(a4: Integer; b4: string);
        procedure test5_invalid(a55: string);
        procedure test6(a6: Integer; b6: string); overload;
        procedure test7;
    end;

    function testRoutine1(a: string): Integer;
    function testRoutine2_invalid(a: string): Integer; overload;
    function testRoutine3_invalid(): Integer;

implementation

{ TTest }

procedure TTest.test1();
begin
end;

procedure TTest.test2_invalid(a2: Integer);
begin
end;

procedure TTest.test3_invalid();
begin
    a3 := 1;
end;

procedure TTest.test4;
begin
    a4 := 4;
end;

procedure TTest.test5_invalid(a5: Integer);
begin
end;

procedure TTest.test6(a6: Integer; b6: string);
begin
    a6 := 6;
end;

procedure TTest.test7();
begin
end;

function testRoutine1(): Integer;
begin
    a;
end;

function testRoutine2_invalid: Integer;
begin
end;

procedure testRoutine3_invalid(a3: Integer);
begin
end;

end.
