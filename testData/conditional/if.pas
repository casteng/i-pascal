{$DEFINE test_def}
type Integer = Integer;
var
  {$if defined(test_undef)}
    inactive1: Integer;
  {$elseif not defined(test_undef)}
    active1: Integer;
  {$endif}

  {$if defined(test_def)}
    active2: Integer;
  {$else}
    inactive2: Integer;
  {$endif}

  {$if defined(test_undef)}
    inactive3: Integer;
  {$elseif not defined(test_undef)}
    active3: Integer;
  {$else}
    inactive3a: Integer;
  {$endif}

  {$if defined(test_undef)}
    inactive4: Integer;
  {$elseif defined(test_undef2)}
    inactive4a: Integer;
  {$else}
    active4: Integer;
  {$endif}

begin
  <caret>
end.