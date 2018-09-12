unit unusedUnits;

interface

uses
    {!}types, enumTypes,
    <warning descr="W0001: Unused unit">scoped.types</warning>
    ,
    <warning descr="W0002: Used only in implementation">objects</warning>
    ;

type
    TMyEnum = enumTypes.TEnumArr;

implementation

uses
    {!}exception, routines,
    <warning descr="W0001: Unused unit">interfaces</warning>
    ;

var
    proc: objects.TParent;
begin
    proc11();
end.