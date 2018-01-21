unit routines;

interface

const kernel = 'kernel32.dll';

type
    TVarData = TVarData;
    PChar = PChar;

    procedure proc1i; external 'kernel32.dll' name 'proc1i';
    procedure proc2i; external kernel name 'proc2i';
    procedure proc3i; external name 'proc3i';

    function test1(a: TVarData): PChar;
    procedure test2(); abstract;

    procedure proc4i; external 'kernel32.dll' index 'proc4i';
    procedure proc5i; external kernel index 'proc5i';
    procedure proc6i; external index 'proc6i';

implementation

procedure VarClearDeep(var V: TVarData); forward;

procedure proc11; external 'kernel32.dll' name 'proc1';
procedure proc2; external kernel name 'proc2';
procedure proc3; external name 'proc3';

function test1();
begin
end;

procedure proc();
begin
    <error descr="Undeclared identifier">Result</error> := 1;
end;

function strFunc(): string;
begin
  Result := '';
end;

procedure proc4; external 'kernel32.dll' index 'proc4';
procedure proc5; external kernel index 'proc5';
procedure proc6; external index 'proc6';

Function f1(P : PChar) : Longint; cdecl; external;
Function f2(P : PChar) : Longint; cdecl; external name 'Fname';
Function f3(P : PChar) : Longint; cdecl; external index 0;
Function f4(arg: TVarData):Integer; Cdecl; External 'libname';
Function f5(P : PChar) : Longint; cdecl; external 'lname' name 'Fname';
Function f6(P : PChar) : Longint; cdecl; external 'lname' index 0;
function ExternalMethod(const SomeString: PChar): Integer; stdcall; external 'cstyle.dll' delayed;
function DllGetDataSnapClassObject(const CLSID, IID: TVarData; var Obj): PChar; cdecl; external 'libmidas.a' dependency 'stdc++', 'dep2';

end.