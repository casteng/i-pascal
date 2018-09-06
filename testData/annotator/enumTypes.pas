unit enumTypes;

interface

type
  Integer = Integer;
  TEnum = (eOne,
      eTwo = 2,
      eThree);
  TEnumArr = array[TEnum.eOne..TEnum.eThree] of TEnum;
  TRec = record
    private type
      TNestedEnum = (CONST1, CONST2);
  end;

implementation

var
  Enum: TEnum;
begin
  TEnum.eTwo;
  eOne;
  TEnum.default;
  Integer.default;
  TRec.TNestedEnum.CONST1;
end.