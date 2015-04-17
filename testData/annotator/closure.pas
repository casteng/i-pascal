uses
sysutils;

begin
  c := TAwaitable.Create(
  function : Integer begin
  Result := 2;
end);
end.