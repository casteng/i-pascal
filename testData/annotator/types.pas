unit types;

interface

type
  Integer = Integer;
  int = type Integer;

  TProc = procedure(a: Integer);
  TFunc = function(a: Int): Int of object;
  TProcOfObj = procedure(a: Integer) of object;

  TEnum = (soDown, soMatchCase, soWholeWord);
  TSet = set of TEnum;

  TArray = array[0..100] of int;
  TEnumArray = array[TEnum] of TSet;

implementation

type
  PInt = ^Int;
  PPInt = ^PInt;

  PComplexType = ^TComplexType;
  TComplexType = array[0..100] of record
    rec: record
    end;
    arr: array of PComplexType;
  end;
end.