{$DEFINE test_def}
type Integer = Integer;
var
{$IF defined(test_def)}
  active1: Integer;
{$ELSEIF defined(test_def)}
  inactive1: Integer;
  {$IFDEF test_def}
    inactive2: Integer;
  {$ENDIF}
{$ELSE}
  inactive3: Integer;
{$ENDIF}

begin
  <caret>
end.