type
  TTest=class
        procedure Test;
  end;

var
  a, b: <error descr="Undeclared identifier">_undeclaredType</error>;
const c = 1;

procedure TTest.Test;
begin
    self.<error descr="Undeclared identifier">self</error>;
end;

begin
end.