type
  TTest = class
    x: Integer;
    FArg1: Integer;
    constructor Create(arg1: Integer);
    property Arg1: Integer read FArg1 write FArg1;
  end;

constructor TTest.Create(arg1: Integer);
begin
  FArg1 :=arg1;
end;

begin
end.
