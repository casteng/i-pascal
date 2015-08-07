unit structTypes;

interface

const kernel = 'kernel32.dll';

type TVarData = TVarData;

    procedure proc1i; external 'kernel32.dll' name 'proc1i';
    procedure proc2i; external kernel name 'proc2i';
    procedure proc3i; external name 'proc3i';

    procedure test1();
    procedure test2(); abstract;

    procedure proc4i; external 'kernel32.dll' index 'proc4i';
    procedure proc5i; external kernel index 'proc5i';
    procedure proc6i; external index 'proc6i';

implementation

procedure VarClearDeep(var V: TVarData); forward;

procedure proc11; external 'kernel32.dll' name 'proc1';
procedure proc2; external kernel name 'proc2';
procedure proc3; external name 'proc3';

procedure test1();
begin

end;

procedure proc4; external 'kernel32.dll' index 'proc4';
procedure proc5; external kernel index 'proc5';
procedure proc6; external index 'proc6';

end.