unit keywordIdentifiers;

interface

type
    &var = &var;
    &type = &var;
    &unit = &type;
    &program = &unit;

    &record = record
        &const: &type;
    end;

    &class = class
    public
        &public: &record;
        procedure &procedure(&out: &type); virtual; abstract;
    end;

implementation

var
    &implementation: &class;

begin
    &implementation.&public;
end.