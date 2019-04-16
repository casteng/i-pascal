unit destructorInherited;
interface

type
    TTest1 = class
        destructor Destroy();
    end;

implementation

destructor TTest1.Destroy();
begin
<warning descr="W0004: No inherited call in destructor">end</warning>;

end.
