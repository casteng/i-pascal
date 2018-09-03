{$DEFINE test_def}
type Integer = Integer;
var
  {$ifdef test_undef}
    inactive1: Integer;
  {$else}
    active1: Integer;
  {$endif}

  {$ifdef test_def}
    active2: Integer;
  {$else}
    inactive2: Integer;
  {$endif}

  {$ifdef test_def}
    active3: Integer;
  {$endif}
begin
  <caret>
end.