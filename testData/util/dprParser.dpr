program dprParser;

{$APPTYPE CONSOLE}
{$R *.res}

{$WARN DUPLICATE_CTOR_DTOR OFF}

uses unit1 in '..\..\Lib\src\unit1.pas'
  ,
  SysUtils,
  unit2 in '..\util\unit2.pas',
  unit3 in 'src\unit3.pas',unit4 in '..\..\testData\unit4.pas',
  unit5 in '..\..\Lib\src\unit5.pas'
  ,
  unit6 in '..\..\Lib\src\unit6.pas', unit7 in '..\..\Lib\src\unit7.pas'
  ,
  unit8 in '..\..\Lib\src\unit8.pas',
  unit9 in '..\..\Lib\src\unit9.pas',  unit10 in '..\..\Lib\src\unit10.pas';


begin
  try
    Readln;
  except
    on E: Exception do
    begin
      Writeln(E.ClassName, ': ', E.Message);
      Readln;
    end;
  end;

end.
