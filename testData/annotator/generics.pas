unit generics;
interface
type
    TEnumerable = TEnumerable;
    Integer = Integer;
    TList<T> = class(TEnumerable<T>)
    public
        constructor Create();
        function GetItem(const Value: T): Integer;
        procedure SetItem(const Value: T);
        property Items[Index: Integer]: T read GetItem write SetItem; default;
    end;
implementation

var
    List: TList<Integer>;

constructor TList<T>.Create();
begin

end;

function TList<T>.GetItem(const Value: T): Integer;
begin

end;

procedure TList<T>.SetItem(const Value: T);
begin

end;

begin
    List := List^.Create;
end.