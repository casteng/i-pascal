unit ipasdbg;

interface

type
  _Arr1 = array[0..0] of ShortInt;
  _Arr2 = array[0..0] of SmallInt;
  _Arr4 = array[0..0] of LongInt;
  _Arr8 = array[0..0] of Int64;
  _PA1 = ^_Arr1;
  _PA2 = ^_Arr2;
  _PA4 = ^_Arr4;
  _PA8 = ^_Arr8;

var
  __a1: _PA1;
  __a2: _PA2;
  __a4: _PA4;
  __a8: _PA8;

implementation

initialization

end.
