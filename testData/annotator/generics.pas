unit generics;
interface
type
    TConstraint = class
      procedure DoSomething();
    end;
    TEnumerable = TEnumerable;
    Integer = Integer;
    TList<T, S: TEnumerable; P: TConstraint> = class(TEnumerable<T>)
    public
        constructor Create();
        function GetItem(const Value: P): Integer;
        procedure SetItem(const Value: T);
        property Items[Index: Integer]: T read GetItem write SetItem; default;
    end;
    TList2<T: TEnumerable, class, constructor>=class
    end;

    TList3<R: record>=class
    end;
implementation

var
    List: TList<Integer>;
    Undecl: <error descr="Undeclared identifier">P</error>;

constructor TList<T, P>.Create();
begin

end;

function TList<T, P>.GetItem(const Value: P): Integer;
begin
    Value.DoSomething();
end;

procedure TList< T,P>.SetItem(const Value: T);
begin

end;

procedure TConstraint.DoSomething();
begin

end;

begin
    List := List^.Create;
end.