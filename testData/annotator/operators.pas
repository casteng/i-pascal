unit operators;

interface

type
    Integer = Integer;
    Single = Single;

    TA = class
    end;

    typeName<T> = record
        class operator conversionOp(a: Integer): Integer;
        class operator in(a: Integer): Integer;
    end;

    TIntArray = array of Integer;
    TMyRec = record
        a: TIntArray;
        class operator := (right: TIntArray): TMyRec;
    end;

    operator Explicit(AVariant: Single) z: Single;

implementation

operator Explicit(AVariant: Single) z: Single;
begin
    Result := Single(AVariant);
end;

operator := (AVariant: Integer) z: Integer;
begin
    z := Integer(AVariant);
end;

class operator typeName<T>.conversionOp(a: Integer): Integer;
begin
end;

class operator typeName<T>.in(a: Integer): Integer;
begin
end;

class operator TMyRec.:= (right: TIntArray): TMyRec;
begin
    result.a := right;
end;

begin
end.
