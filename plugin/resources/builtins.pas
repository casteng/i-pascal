{
Built-in identifiers unit.
You may edit this file but any modifications will be discarded after IDE restart.
}
unit $builtins;

interface

const
    MaxInt = 2147483647;
    MaxLongint = 2147483647;

type
    string = string;
    Integer = -2147483648..2147483647;
    Byte = 0..255;
    ShortInt = -128..127;
    Word = 0..65535;
    SmallInt = -32768..32767;
    LongWord = 0..4294967295;
    LongInt = -2147483648..2147483647;
    QWord = 0..18446744073709551615;
    Int64 = -9223372036854775808..9223372036854775807;
    UInt64 = UInt64;
    Cardinal = Cardinal;
    Boolean = false..true;
    Boolean16 = false..true;
    Boolean32 = false..true;
    Boolean64 = false..true;
    ByteBool = false..true;
    WordBool = false..true;
    LongBool = false..true;
    QWordBool = false..true;
    Char = Char;
    WideChar = WideChar;
    ShortString = ShortString;
    AnsiString = AnsiString;
    WideString = WideString;
    UnicodeString = UnicodeString;
    OpenString = OpenString;
    Single = Single;
    Double = Double;
    Extended = Extended;
    CExtended = CExtended;
    Currency = Currency;
    Pointer = Pointer;
    NearPointer = NearPointer;
    NearCsPointer = NearCsPointer;
    NearDsPointer = NearDsPointer;
    NearSsPointer = NearSsPointer;
    NearEsPointer = NearEsPointer;
    NearFsPointer = NearFsPointer;
    NearGsPointer = NearGsPointer;
    Variant = Variant;
    OleVariant = OleVariant;
    Comp = Comp;
    Text = Text;
    TypedFile = TypedFile;
    Real = Real;
    NativeInt = NativeInt;
    NativeUInt = NativeUInt;
    PWideChar = ^WideChar;
    AnsiChar = AnsiChar;
    PAnsiChar = ^AnsiChar;
    PChar = ^Char;
    PShortString = ^ShortString;
    TextFile = TextFile;

const
    PI: Extended = 3.1415926535897932385;

    function Abs(X: Integer): Integer; overload;
    function Abs(X: Extended): Extended; overload;
    function Addr(const X): Pointer;
    function Append(var F: Text): Integer;
    function Assert(expr: Boolean; const msg: string): boolean; overload;
    function Assert(expr: Boolean; const msg: string): boolean; overload;
    function Assign(var t: Text; const s: PChar): Integer;
    function Assigned(const P): Boolean;
    function AssignFile(var F: File; FileName: String): Integer; overload;
    function AssignFile(var F: File; FileName: String; CodePage: Word): Integer; overload;
    function Append(var t: Text): Integer;
    function BlockRead(var f: File; buffer: Pointer; recCnt: Longint; var recsRead: Longint): Longint;
    function BlockWrite(var f: File; buffer: Pointer; recCnt: Longint; var recsWritten: Longint): Longint;
    function Chr(X: Byte): Char;
    function Close(var t: Text): Integer;
    function Concat(S1, S2: string): string;
    function Copy(S; Index, Count: Integer): string;
    procedure Dec(var X); overload;
    procedure Dec(var X; N: longint); overload;
    procedure Delete(var S: string; Index, Count : Integer);
    procedure Dispose(var P: Pointer);
    function EofFile(var f: File): Boolean;
    function EofText(var t: Text): Boolean;
    function Eoln(var t: Text): Boolean;
    procedure Erase(var f: File);
    procedure Exclude(var S: set of Byte; element: Byte);
    function FilePos(var f: File): Longint;
    function FileSize(var f: File): Longint;
    function Flush(var t: Text): Integer;
    procedure FillChar(var X; Count : Integer; Value : Byte);
    procedure Finalize(var V); overload;
    procedure Finalize(var V; Count: NativeUInt); overload;
    function FreeMem(P: Pointer): Integer;
    procedure GetDir(sDrive: Byte; var sDir: string);
    function GetMem(Size: Integer): Pointer;
    procedure Halt(Code: Integer); overload;
    procedure Halt(); overload;
    function Hi(X: Integer): Byte;
    function High(const X): Integer;
    procedure Inc(var X); overload;
    procedure Inc(var X; N: longint); overload;
    procedure Include(var S: set of Byte; element: Byte);
    procedure Initialize(var V); overload;
    procedure Initialize(var V; Count: NativeUInt); overload;
    procedure Insert(Source : string; var S : string; Index : Integer);

    function Length(const S: string): Integer;
    function Lo(X: Integer): Byte;
    function Low(const X): Integer;
    procedure New(var X: Pointer);
    function Odd(X: Longint): Boolean; overload;
    function Ord(X): Longint; overload;

    function Pred(const X): Variant;
    function Ptr(Address: Integer): Pointer;
    procedure ReadLn(var t: Text);
    function ReallocMem(var P: Pointer; NewSize: Integer): Pointer;
    procedure Rename(var f: File; newName: PChar);
    procedure Reset(var F: File); overload;
    procedure Reset(var F: File; RecSize: Integer); overload;
    procedure Rewrite(var F: File); overload;
    procedure Rewrite(var F: File; RecSize: Integer); overload;
    function Round(X: Extended): Int64;
    procedure RunError(); overload;
    procedure RunError(ErrorCode: Byte); overload;
    procedure SetLength(var S; Length1: Integer);
    procedure SetString(var S: string; c : PChar; Length: Integer);
    function SizeOf(const X) : Integer;
    function Slice(var A: array of Variant; Count: Integer): array of Variant;
    function Sqr(X: Extended): Extended;
    procedure Str(const X; var S);
    function Succ(const X): Variant;
    function Swap(X: Integer): Integer;
    function Trunc(X: Extended): Int64;
    procedure Truncate(var f: File);
    function TypeHandle(const T): Pointer;
    function TypeInfo(const T): Pointer;
    function TypeOf(const X): Pointer;
    procedure Val(const S: string; var Result; var Code: integer);
    procedure VarCast(var Dest: Variant; Source: Variant; VarType: Integer);
    procedure VarCopy(var Dest: Variant; Source: Variant);

    procedure Mark; deprecated;
    procedure Release; deprecated;

    procedure Write();
    procedure WriteLn();

    procedure Seek(var f: File; recNum: Cardinal);
    function SeekEof(): Boolean; overload;
    function SeekEof(var t: Text): Boolean; overload;
    function SeekEoln(): Boolean; overload;
    function SeekEoln(var t: Text): Boolean; overload;
    procedure SetTextBuf(var t: Text; p: Pointer; size: Longint);

    function StrLong(val, width: Longint): ShortString;
    function Str0Long(val: Longint): ShortString;

// Include-based template stub classes
type
    __Parent = class()
        constructor Create();
        destructor Destroy();
        procedure Free();
    end;

    _GenVector = class(__Parent)
        procedure SetValue(Index: __CollectionIndexType; const e: _VectorValueType);
        procedure SetCapacity(const ACapacity: __CollectionIndexType);
        // Increases the capacity of the list to ensure that it can hold at least the number of elements specified
        procedure EnsureCapacity(ACount: __CollectionIndexType);
        // Returns the number of elements in the collection
        function GetCount(): __CollectionIndexType;
        // Sets the number of elements in the collection
        procedure SetCount(const ACount: __CollectionIndexType);
        // Returns True if the collection contains no elements
        function IsEmpty(): Boolean;
        // Returns True if the collection contains the specified element
        function Contains(const e: _VectorValueType): Boolean;
        // Calls the delegate for each element in the collection
        procedure ForEach(Delegate: _VectorDelegate; Data: Pointer); overload;
        // Calls the delegate for each element in the collection
        procedure ForEach(Callback: _VectorCallback; Data: Pointer); overload;
        // Searches for element which satisfies the condition _VectorFound(element, Pattern) and returns its index or -1 if no such element.
        function Find(const Pattern: _VectorSearchType): __CollectionIndexType;
        // Searches for element which satisfies the condition _VectorFound(element, Pattern) starting from last one and returns its index or -1 if no such element.
        function FindLast(const Pattern: _VectorSearchType): __CollectionIndexType;
        // Appends the element as the last element of the vector and returns True
        function Add(const e: _VectorValueType): Boolean;
        {/ Removes the specified element from the collection.
           Returns True if the collection contained the element./}
        function Remove(const e: _VectorValueType): Boolean;
        // Removes all elements from the collection
        procedure Clear();
        // Number of elements
        property Count: __CollectionIndexType read FCount write SetCount;
        {/ Returns the element at the specified position in the list.
           Throws an error on invalid index if dsRangeCheck was included in the list options before instantiation. }
        function Get(Index: __CollectionIndexType): _VectorValueType;
        {/ Returns the address of the element at the specified position in the list.
           Throws an error on invalid index if dsRangeCheck was included in the list options before instantiation. }
        function GetPtr(Index: __CollectionIndexType): _PVectorValueType;
        {/ Replaces the element at the specified position in the list with the specified element.
           Returns the element previously at the specified position.
           Throws an error on invalid index if dsRangeCheck was included in the list options when instantiation. }
        function Put(Index: __CollectionIndexType; const e: _VectorValueType): _VectorValueType;
        {/ Inserts the element at the specified position in the list shifting the element currently at that
           position (if any) and any subsequent elements to the right.
           Throws an error on invalid index if dsRangeCheck was included in the list options when instantiation. }
        procedure Insert(Index: __CollectionIndexType; const e: _VectorValueType);
        {/ Removes the element at the specified position in the list shifting any subsequent elements
           to the left.
           Returns the element that was removed from the list. }
        function RemoveBy(Index: __CollectionIndexType): _VectorValueType;
        {/ Returns the index of the first occurrence of the specified element in the list,
           or -1 if the list does not contain the element. }
        function IndexOf(const e: _VectorValueType): __CollectionIndexType;
        {/ Returns the index of the last occurrence of the specified element in the list,
           or -1 if the list does not contain the element. }
        function LastIndexOf(const e: _VectorValueType): __CollectionIndexType;
        // Values retrieved by index
        property Values[Index: __CollectionIndexType]: _VectorValueType read Get write SetValue; default;
        // Pointer to values retrieved by index
        property ValuesPtr[Index: __CollectionIndexType]: _PVectorValueType read GetPtr;
        // Number of elements which the collection able to hold without memory allocations
        property Capacity: __CollectionIndexType read FCapacity write SetCapacity;
    end;
    
    _GenHashMap = class(__Parent)
        function GetLoadFactor(): Single;

    protected
        FValues: _HashMapKeys;
        strict protected
        // Capacity of the hash map. Should be a power of 2.
        FCapacity,
        // Capacity mask
        FMask: __CollectionIndexType;
        // Threshold of number of entries to capacity ratio after which capacity doubles. If zero automatic resizing doesn't occur.
        FMaxLoadFactor: Single;
        // Grow step of bucket array
        FBucketGrowStep: __CollectionIndexType;
        // Number of entries
        FCount: __CollectionIndexType;
        function GetIndexInBucket(const Key: _HashMapKeyType; out BucketIndex: __CollectionIndexType): __CollectionIndexType;
        function GetValue(const Key: _HashMapKeyType): _HashMapValueType;
        procedure SetValue(const Key: _HashMapKeyType; const Value: _HashMapValueType);
        procedure SetCapacity(ACapacity: __CollectionIndexType);
    public
        constructor Create(); overload;
        // Create a map instance with the specified initial capacity
        constructor Create(Capacity: __CollectionIndexType); overload;
        destructor Destroy(); override;
        // Returns a forward iterator over map keys collection
        function GetKeyIterator(): _GenHashMapKeyIterator;
        // Returns True if the hash map contains the key
        function ContainsKey(const Key: _HashMapKeyType): Boolean;
        // Returns True if the hash map contains the value
        function ContainsValue(const Value: _HashMapValueType): Boolean;
        // Calls a delegate for each value stored in the map
        procedure ForEach(Delegate: _HashMapDelegate; Data: Pointer);
        // Returns True if the collection contains no elements
        function IsEmpty(): Boolean;
        // Removes all elements from the collection
        procedure Clear();
        // Values retrieved by pointer key
        property Values[const Key: _HashMapKeyType]: _HashMapValueType read GetValue write SetValue; default;
        // Determines hash function values range which is currently used.
        property Capacity: __CollectionIndexType read FCapacity;
        // Threshold of number of entries to capacity ratio after which capacity doubles. If zero automatic resizing doesn't occur.
        property MaxLoadFactor: Single read FMaxLoadFactor write FMaxLoadFactor;
        // Current number of entries to capacity ratio
        property LoadFactor: Single read GetLoadFactor;
        // Grow step of bucket array
        property BucketGrowStep: __CollectionIndexType read FBucketGrowStep write FBucketGrowStep;
        // Number of entries
        property Count: __CollectionIndexType read FCount write FCount;
    end;

    _GenHashMapKeyIterator = class(__Parent)
        // Advances the iterator to next item and returns True on success or False if no items left
        function GoToNext(): Boolean;
        // Returns current item performing no iterator state changes
        function Current(): _HashMapKeyType;
        // Returns True if there is more items
        function HasNext(): Boolean;
        // Advances the iterator to next item and returns it.
        // If no items left nil be returned for nullable collection (dsNullable option is defined) or error generated otherwise.
        function Next(): _HashMapKeyType;
    end;

implementation

end.
