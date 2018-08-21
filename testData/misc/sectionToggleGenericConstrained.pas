unit sectionToggleGenericConstrained;

interface

type
    TTest<T: class, P: record> = class
        constructor Create(ac1, ac2: T);
        destructor Destroy(); override;
        class procedure test1();
        function test4(a4: T; b4: P): T;
        procedure test6(a6: T; b6: P); overload;
        procedure test7;
    end;

implementation

{ TTest }

constructor TTest<T, P>.Create;
begin
    ac2 := 1;
end;

destructor TTest<T, P>.Destroy();
begin
end;

class procedure TTest<T, P>.test1();
begin
end;

procedure TTest<T, P>.test4;
begin
end;

procedure TTest<T, P>.test6(a6: T; b6: P);
begin
end;

procedure TTest.test7();
begin
end;

end.
