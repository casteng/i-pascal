{
Built-in identifiers unit.
You may edit this file but any modifications will be discarded after IDE restart.
}
unit $builtins;

interface

type string = string;

type Integer = -2147483648..2147483647;

type Byte = 0..255;

type ShortInt = -128..127;

type Word = 0..65535;

type SmallInt = -32768..32767;

type LongWord = 0..4294967295;

type LongInt = -2147483648..2147483647;

type QWord = 0..18446744073709551615;

type Int64 = -9223372036854775808..9223372036854775807;

type Cardinal = Cardinal;

type Boolean = false..true;

type Boolean16 = false..true;

type Boolean32 = false..true;

type Boolean64 = false..true;

type ByteBool = false..true;

type WordBool = false..true;

type LongBool = false..true;

type QWordBool = false..true;

type Char = Char;

type WideChar = WideChar;

type ShortString = ShortString;

type AnsiString = AnsiString;

type WideString = WideString;

type UnicodeString = UnicodeString;

type OpenString = OpenString;

type Single = Single;

type Double = Double;

type Extended = Extended;

type CExtended = CExtended;

type Currency = Currency;

type Pointer = Pointer;

type NearPointer = NearPointer;

type NearCsPointer = NearCsPointer;

type NearDsPointer = NearDsPointer;

type NearSsPointer = NearSsPointer;

type NearEsPointer = NearEsPointer;

type NearFsPointer = NearFsPointer;

type NearGsPointer = NearGsPointer;

type Variant = Variant;

type OleVariant = OleVariant;

type Comp = Comp;

type Text = Text;

type TypedFile = TypedFile;

type Real = Real;

type NativeInt = NativeInt;

type NativeUInt = NativeUInt;

type PWideChar = ^WideChar;

type AnsiChar = AnsiChar;

type PAnsiChar = ^AnsiChar;

type PChar = ^Char;

type TextFile = TextFile;

        // Include-based template stub classes
    __Parent = class()
        constructor Create();
        destructor Destroy();
        procedure Free();
    end;

    _GenVector = class(__Parent)
    end;
    _GenHashMap = class(__Parent)
    end;
    _GenHashMapKeyIterator = class(__Parent)
    end;
    _GenVector = class(__Parent)
    end;

    procedure assert(Value: Boolean);
    function SizeOf(): Integer;
    function Ord(const V): Integer;
    function Str(const V): string;
    function Val(const S): Integer;
    procedure Write();
    procedure WriteLn();
    procedure Read();
    procedure ReadLn();
    function Length(const S): Integer;
    procedure SetLength(var s; Len: Integer);
    function Copy(): string;
    function Concat(): string;
    procedure Slice();
    procedure Inc();
    procedure Dec();
    function High(): Integer;
    function Low(): Integer;
    function Succ(): Integer;
    function Pred(): Integer;
    procedure Include();
    procedure Exclude();
    function Assigned(): boolean;
    procedure SetString();
    procedure New();
    procedure Dispose();
    procedure AssignFile();
    procedure CloseFile();
    procedure Finalize();
    procedure Initialize();

implementation

end.
