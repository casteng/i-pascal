unit sectionToggle;

interface

type
    TTest = class
        procedure test1();
        procedure test2_invalid();
        procedure test3(a3: Integer);
        procedure test4(a4: Integer; b4: string);
    end;

implementation

{ TTest }

procedure TTest.test1();
begin
end;

procedure TTest.test2_invalid(a2: Integer);
begin
end;

procedure TTest.test3();
begin
end;

procedure TTest.test4;
begin
end;

end.
