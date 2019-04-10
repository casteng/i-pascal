type
  TClosure = reference to procedure(const param: Integer);

procedure test(a: TClosure);
begin
end;

var c: TClosure;

begin
  c := test(
  function : Integer begin
    Result := 2;
  end
  );
end.