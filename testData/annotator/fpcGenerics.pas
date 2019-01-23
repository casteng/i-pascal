unit fpcGenerics;
interface
type
    TInt = TInt;
    generic TGList<T> = class
        function GetCurrent: T;
    end;
    TIntList = specialize TGList<TInt>;
implementation

generic function Add<T>(aLeft, aRight: T): T;
begin
    Result := aLeft + aRight;
end;

function TGList.GetCurrent: T;
begin
    Result := T();
end;

end.