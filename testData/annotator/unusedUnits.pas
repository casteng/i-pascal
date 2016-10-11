unit unusedUnits;

interface

uses
    {!}types, enumTypes,
    <warning descr="Unused unit">scoped.types</warning>
    ,
    <warning descr="Used only in implementation">objects</warning>
    ;

type
    TMyEnum = enumTypes.TEnumArr;

implementation

uses
    {!}exception, routines,
    <warning descr="Unused unit">interfaces</warning>
    ;

var
    proc: objects.TParent;
begin
    proc11();
end.