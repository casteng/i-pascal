unit routinesForward;

interface

type
    T3 = T3;

    procedure proc2(a: T3);

implementation

type
    T = T;
    T2 = T2;

procedure proc1(a: T); forward;

procedure proc1(a: T);
var
    l1: T;
begin
    l1 := a;
end;

procedure proc2(a: T);
var
    l2: T;
begin
    l2 := a;
end;

procedure proc2(a: T2);
var
    l3: T;
begin
    l3 := a;
end;

procedure proc2(a: T3);
var l4: T3;
begin
    l4 := a;
end;

begin
    proc1(1);
end.