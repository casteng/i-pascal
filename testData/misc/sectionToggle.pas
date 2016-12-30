unit sectionToggle;

interface

type
    TTest = class
        constructor Create(ac1, ac2: Integer);
        class procedure test1();
        procedure test2();
        procedure test3(a3: Integer); overload;
        procedure test4(a4: Integer; b4: string);
        procedure test5(a55: string);
        procedure test6(a6: Integer; b6: string); overload;
        procedure test7;
    end;

    function testRoutine1(a: string): Integer;
    function testRoutine2(a: string): Integer; overload;
    function testRoutine3(): Integer;

implementation

{ TTest }

constructor TTest.Create;
begin
end;

class procedure TTest.test1();
begin
end;

procedure TTest.test2(a2: Integer);
begin
end;

procedure TTest.test3();
begin
end;

procedure TTest.test4;
begin
end;

procedure TTest.test5(a5: Integer);
begin
end;

procedure TTest.test6(a6: Integer; b6: string);
begin
end;

procedure TTest.test7();
begin
end;

function testRoutine1(): Integer;
begin
end;

function testRoutine2: Integer;
begin
end;

procedure testRoutine3(a3: Integer);
begin
end;

end.
