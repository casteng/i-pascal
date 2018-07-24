unit fpcGenerics;
interface
type
    TInt = TInt;
    generic TGList<T> = class
        function GetCurrent: T;
    end;
    TIntList = specialize TGList<TInt>;
implementation

function TGList.GetCurrent: T;
begin
    Result := T();
end;

end.