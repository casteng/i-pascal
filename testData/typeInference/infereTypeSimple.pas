unit infereTypeSimple;

interface

type
  TArr = array[0..1] of Integer;

type
  TEnum = (V1, V2);
  PEnum = ^TEnum;

var
  int: Integer;
  enum: TEnum;
  enumPtr: PEnum;
  arrArr: array of TArr;

implementation

begin
  int;
  enum;
  enumPtr;
  arrArr;
  1;
  $FF;
  1.0;
  'test';
  True;
  Nil;
end.