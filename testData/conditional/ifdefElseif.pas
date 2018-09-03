{$DEFINE test_def}
type Integer = Integer;
var
  {$ifdef test_undef}
    inactive1: Integer;
  {$elseif not defined(test_undef)}
    active1: Integer;
  {$else}
    inactive1a: Integer;
  {$endif}

  {$ifdef test_undef}
    inactive2: Integer;
  {$elseif not defined(test_def)}
    inactive2a: Integer;
  {$else}
    active2: Integer;
  {$endif}

begin
  <caret>
end.