unit generics;
interface
type
    TEnumerable = TEnumerable;
    Integer = Integer;
    TList<T: TEnumerable, P:A> = class(TEnumerable<T>)
    public
        constructor Create();
        function GetItem(const Value: T): Integer;
        procedure SetItem(const Value: T);
        property Items[Index: Integer]: T read GetItem write SetItem; default;
    end;
    TList2<T: TEnumerable, class, constructor>=class
    end;

    TList3<record>=class
    end;
implementation

var
    List: TList<Integer>;

constructor TList<T, P>.Create();
begin

end;

function TList<T, P>.GetItem(const Value: T): Integer;
begin

end;

procedure TList< T,P>.SetItem(const Value: T);
begin

end;

begin
    List := List^.Create;
end.