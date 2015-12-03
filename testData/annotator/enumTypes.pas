unit enumTypes;

interface

type
  Integer = Integer;
  TEnum = (eOne, eTwo = 2, eThree);
  TEnumArr = array[TEnum.eOne..TEnum.eThree] of TEnum;

implementation

var
  Enum: TEnum;
begin
  TEnum.eTwo;
  TEnum.default;
  Integer.default;
end.