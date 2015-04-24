type
  TClosure = reference to procedure(const param: Integer);
var
c: TClosure;
begin
  c := _GenVector.Create(
  function : Integer begin
    Result := 2;
  end
  );
end.