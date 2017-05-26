unit sectionToggle;

interface

type
    TTest = class
        constructor Create(ac1, ac2: Integer);
        constructor Init(ac1: Integer);
        destructor Destroy(); override;
        class procedure test1();
        procedure test2_invalid();
        procedure test3_invalid(a3: Integer); overload;
        function test4(a4: Integer; b4: string): string;
        procedure test5_invalid(a55: string);
        procedure test6(a6: Integer; b6: string); overload;
        procedure test7;
        function test8_invalid(a4: Integer; b4: string): string;
    end;

    function testRoutine1(a: string): Integer;
    function testRoutine2_invalid(a: string): Integer; overload;
    function testRoutine3_invalid(): Integer;
    function testRoutine4_invalid(): Integer;
    function testRoutine5(a5: Integer): string;

implementation

{ TTest }

constructor TTest.Create;
begin
end;

constructor TTest.Init(ac1: Integer);
begin
end;

destructor TTest.Destroy();
begin
end;

class procedure TTest.test1();
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

function TTest.test8_invalid(a4: Integer; b4: string): Integer;
begin
end;

function testRoutine1();
begin
    a;
end;

function testRoutine2_invalid: Integer;
begin
end;

procedure testRoutine3_invalid(a3: Integer);
begin
end;

function testRoutine4_invalid(): string;
begin
end;

function testRoutine5;
    function nested_invalid(): string;
    begin

    end;

begin
end;

end.
