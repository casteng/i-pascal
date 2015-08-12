unit structTypes;

interface

type
    Integer = Integer;
    Single = Single;

    TA = class
    end;

    typeName = record
        class operator conversionOp(a: Integer): Integer;
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

class operator typeName.conversionOp(a: Integer): Integer;
begin
end;

begin
end.