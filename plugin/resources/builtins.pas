{
Built-in identifiers unit.
You may edit this file but any modifications will be discarded after IDE restart.
Feel free to suggest additions to this file.
}
unit $builtins;
{$DEFINE _IDE_PARSER_}                // This define is always defined in I-Pascal and can be used to fix include-related parsing issues
{$DEFINE _IDE_DISABLE_CONDITIONALS_}  // This define can be defined if it's desirable for I-Pascal to ignore conditional compilation e.g. to fix parsing issues

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
    // Returns an absolute value.
    function Abs(X: Integer): Integer; overload;
    // Returns an absolute value.
    function Abs(X: Extended): Extended; overload;
    // Returns a pointer to a specified object.
    function Addr(const X): Pointer;
    // Prepares an existing file for adding text to the end.
    function Append(var F: Text): Integer;
    // Tests whether a Boolean expression is true.
    function Assert(expr: Boolean; const msg: string): boolean; overload;
    // Tests whether a Boolean expression is true.
    function Assert(expr: Boolean; const msg: string): boolean; overload;
    // Associates the name of an external file with a file variable.
    function Assign(var t: Text; const s: PChar): Integer;
    // Tests for a nil (unassigned) pointer or procedural variable.
    function Assigned(const P): Boolean;
    // Associates the name of an external file with a file variable.
    function AssignFile(var F: File; FileName: String): Integer; overload;
    // Associates the name of an external file with a file variable.
    function AssignFile(var F: File; FileName: String; CodePage: Word): Integer; overload;

    // AtomicCmpExchange is used for comparing and exchanging memory values.
    function AtomicCmpExchange(var Target; NewValue: Integer; Comparand: Integer): Integer; overload;
    // AtomicCmpExchange is used for comparing and exchanging memory values.
    function AtomicCmpExchange(var Target; NewValue: Integer; Comparand: Integer; out Succeeded: Boolean): Integer; overload;
    // AtomicDecrement is used for decrementing memory values.
    function AtomicDecrement(var Target; Decrement: Integer = 1): Int64;
    // AtomicExchange is used for exchanging memory values.
    function AtomicExchange(var Target; Value: Integer): Int64;
    // AtomicIncrement is used for incrementing memory values.
    function AtomicIncrement(var Target; Increment: Integer = 1): Int64;

    // Reads one or more records from an open file into a variable.
    function BlockRead(var f: File; buffer: Pointer; recCnt: Longint; var recsRead: Longint): Longint;
    // Writes one or more records from a variable to an open file.
    function BlockWrite(var f: File; buffer: Pointer; recCnt: Longint; var recsWritten: Longint): Longint;
    // Returns the character for a specified ASCII value.
    function Chr(X: Byte): Char;
    // Terminates the association between a file variable and an external file.
    function Close(var t: Text): Integer;
    // Terminates the association between file variable and an external disk file.
    procedure CloseFile(var F: File);
    // Concatenates two or more strings into one.
    function Concat(S1, S2: string): string;
    // Returns a substring of a string or a segment of a dynamic array.
    function Copy(S; Index, Count: Integer): string;
    // Decrements a variable by 1 or N.
    procedure Dec(var X); overload;
    // Decrements a variable by 1 or N.
    procedure Dec(var X; N: longint); overload;
    // Removes a substring from a string.
    procedure Delete(var S: string; Index, Count : Integer);
    // Releases memory allocated for a dynamic variable.
    procedure Dispose(var P: Pointer);
    // Tests whether the file position is at the end of a file.
    function Eof(var f: File): Boolean;
    // Tests whether the file position is at the end of a file.
    function EofFile(var f: File): Boolean;
    // // Tests whether the text file position is at the end of a file.
    function EofText(var t: Text): Boolean;
    // Tests whether the file pointer is at the end of a line.
    function Eoln(var t: Text): Boolean;
    // Deletes an external file.
    procedure Erase(var f: File);
    // Removes an element from a Pascal set.
    procedure Exclude(var S: set of Byte; element: Byte);
    // Cancels the construction of an object (Turbo Pascal object model).
    procedure Fail;
    // Returns the current file position.
    function FilePos(var f: File): Longint;
    // Returns the number of records in a file.
    function FileSize(var f: File): Longint;
    // Fills contiguous bytes with a specified value.
    procedure FillChar(var X; Count : Integer; Value : Byte);
    // Uninitializes a dynamically allocated variable.
    procedure Finalize(var V); overload;
    // Uninitializes a dynamically allocated variable.
    procedure Finalize(var V; Count: NativeUInt); overload;
    // Empties the buffer of a text file opened for output.
    function Flush(var t: Text): Integer;
    // FreeMem frees a memory block.
    function FreeMem(P: Pointer): Integer;
    // Returns the current directory.
    procedure GetDir(sDrive: Byte; var sDir: string);
    // GetMem allocates a memory block.
    function GetMem(Size: Integer): Pointer;
    // Initiates abnormal termination of a program.
    procedure Halt(Code: Integer); overload;
    // Initiates abnormal termination of a program.
    procedure Halt(); overload;
    // Returns the high-order byte of X as an unsigned value.
    function Hi(X: Integer): Byte;
    // Returns the highest value in the range of an argument.
    function High(const X): Integer;
    // Increments an ordinal value by one or N.
    procedure Inc(var X); overload;
    // Increments an ordinal value by one or N.
    procedure Inc(var X; N: longint); overload;
    // Adds an element to a Delphi set.
    procedure Include(var S: set of Byte; element: Byte);
    // Initializes a dynamically allocated variable.
    procedure Initialize(var V); overload;
    // Initializes a dynamically allocated variable.
    procedure Initialize(var V; Count: NativeUInt); overload;
    // Inserts a substring into a string beginning at a specified point.
    procedure Insert(Source : string; var S : string; Index : Integer);

    // Returns the number of characters in a string or elements in an array.
    function Length(const S: string): Integer;
    // Returns the low order Byte of argument X.
    function Lo(X: Integer): Byte;
    // Returns the lowest value in a range.
    function Low(const X): Integer;

    // Enforces an ordering constraint on memory operations.
    procedure MemoryBarrier;
    // Returns the value of this expression: Int64((Int128(AValue) * Int128(AMul)) div Int128(ADiv)).
    function MulDivInt64(AValue, AMul, ADiv: Int64): Int64; overload;
    // Returns the value of this expression: Int64((Int128(AValue) * Int128(AMul)) div Int128(ADiv)).
    function MulDivInt64(AValue, AMul, ADiv: Int64; out Remainder: Int64): Int64; overload;

    // Creates a new dynamic variable and sets P to point to it.
    procedure New(var X: Pointer);
    // Returns true if argument is an odd number.
    function Odd(X: Longint): Boolean; overload;
    // Returns the ordinal value of an ordinal-type expression.
    function Ord(X): Longint; overload;

    // Returns the predecessor of the argument.
    function Pred(const X): Variant;
    // Converts a specified address to a pointer.
    function Ptr(Address: Integer): Pointer;
    // Read reads data from a file.
    procedure Read(var t: Text);
    // Read reads a line of text from a file.
    procedure ReadLn(var t: Text);
    // ReallocMem reallocates a memory block.
    function ReallocMem(var P: Pointer; NewSize: Integer): Pointer;
    // Changes the name of an external file.
    procedure Rename(var f: File; newName: PChar);
    // Opens an existing file.
    procedure Reset(var F: File); overload;
    // Opens an existing file.
    procedure Reset(var F: File; RecSize: Integer); overload;
    // Creates a new file and opens it.
    procedure Rewrite(var F: File); overload;
    // Creates a new file and opens it.
    procedure Rewrite(var F: File; RecSize: Integer); overload;
    // Returns the value of X rounded to the nearest whole number.
    function Round(X: Extended): Int64;
    // Stops execution and generates a runtime error.
    procedure RunError(); overload;
    // Stops execution and generates a runtime error.
    procedure RunError(ErrorCode: Byte); overload;
    // Moves the current position of a file to a specified component.
    procedure Seek(var f: File; recNum: Cardinal);
    // Returns the end-of-file status of a file, ignoring whitespace.
    function SeekEof(): Boolean; overload;
    // Returns the end-of-file status of a file, ignoring whitespace.
    function SeekEof(var t: Text): Boolean; overload;
    // Returns the end-of-line status of a file, ignoring whitespace.
    function SeekEoln(): Boolean; overload;
    // Returns the end-of-line status of a file, ignoring whitespace.
    function SeekEoln(var t: Text): Boolean; overload;
    // Sets the length of a string or dynamic-array variable.
    procedure SetLength(var S; Length1: Integer);
    // Sets the contents and length of the given string.
    procedure SetString(var S: string; c : PChar; Length: Integer);
    // Assigns an I/O buffer to a text file.
    procedure SetTextBuf(var t: Text; p: Pointer; size: Longint);
    // Returns the number of bytes occupied by a variable or type.
    function SizeOf(const X) : Integer;
    // Returns a sub-section of an array.
    function Slice(var A: array of Variant; Count: Integer): array of Variant;
    // Returns the square of a number.
    function Sqr(X: Extended): Extended;
    // Formats a string and returns it to a variable.
    procedure Str(const X; var S);
    // Returns the successor of an argument.
    function Succ(const X): Variant;
    // Exchanges high order byte with the low order byte of an integer or word.
    function Swap(X: Integer): Integer;
    // Truncates a real number to an integer.
    function Trunc(X: Extended): Int64;
    // Deletes all the records after the current file position.
    procedure Truncate(var f: File);
    // Returns the RTTI information for a given type.
    function TypeHandle(const T): Pointer;
    // Returns the RTTI information for a given type.
    function TypeInfo(const T): Pointer;
    // Deprecated routine.
    function TypeOf(const X): Pointer; deprecated;
    // Converts a string to a numeric representation.
    procedure Val(const S: string; var Result; var Code: integer);
    // Resizes a Variant array.
    procedure VarArrayRedim(var A: Variant; HighBound: Integer);
    // Converts a variant to specified type.
    procedure VarCast(var Dest: Variant; Source: Variant; VarType: Integer);
    // Empties a Variant so that it is unassigned.
    procedure VarClear(var V: Variant);
    // Copies a Variant to another Variant.
    procedure VarCopy(var Dest: Variant; Source: Variant);
    // Writes to either a typed file or a text file.
    procedure Write();
    // Writes to a text file and adds an end-of-line marker.
    procedure WriteLn();


    procedure Mark; deprecated;
    procedure Release; deprecated;
    function StrLong(val, width: Longint): ShortString;
    function Str0Long(val: Longint): ShortString;

    {$IFDEF DELPHI}
    // Added for D7 compatibility
    function Sin(x: Extended): Extended;
    function Cos(x: Extended): Extended;
    function Exp(x: Extended): Extended;
    function Int(x: Extended): Extended;
    function Frac(x: Extended): Extended;
    {$ENDIF}

    // Undocumented
    // Returns the zero representation of type identifier T.
    function Default(const T): T;
    // True if T is a interface, string or dynamic array, or a record containing such. A class containing a managed type will return false.
    function IsManagedType(const T): Boolean;
    function HasWeakRef(const T): Boolean;
    // Does the same thing as PTypeInfo(System.TypeInfo(T))^.Kind;, however because it is a compiler intrinsic the function is resolved at compiletime and conditional code that evaluates to false will be stripped by the compiler.
    function GetTypeKind(const T): TTypeKind;
    { True if Value is a constant, false if not.
      This helps the compiler to eliminate dead code because the function is evaluated at compile time.
      This is really only useful in inline functions, where it allows for shorter generated code. }
    function IsConstValue(const Value): Boolean;

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
        procedure EnsureCapacity(ASize: __CollectionIndexType);
        // Returns the number of elements in the collection
        function GetSize(): __CollectionIndexType;
        // Sets the number of elements in the collection
        procedure SetSize(const ASize: __CollectionIndexType);
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
        // Number of elements
        property Size: __CollectionIndexType read FSize write SetSize;
        // Values retrieved by index
        property Values[Index: __CollectionIndexType]: _VectorValueType read Get write SetValue; default;
        // Pointer to values retrieved by index
        property ValuesPtr[Index: __CollectionIndexType]: _PVectorValueType read GetPtr;
        // Number of elements which the collection able to hold without memory allocations
        property Capacity: __CollectionIndexType read FCapacity write SetCapacity;
    end;

    _HashMapPair = record Key: _HashMapKeyType; Value: _HashMapValueType; end;

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
        FSize: __CollectionIndexType;
        function GetIndexInBucket(const Key: _HashMapKeyType; out BucketIndex: __CollectionIndexType): __CollectionIndexType;
        function GetValue(const Key: _HashMapKeyType): _HashMapValueType;
        procedure SetValue(const Key: _HashMapKeyType; const Value: _HashMapValueType);
        procedure SetCapacity(ACapacity: __CollectionIndexType);
    public
        constructor Create(); overload;
        // Create a map instance with the specified initial capacity. It's recommended to specify capacity to avoid expensive resizing of internal data structures.
        constructor Create(Capacity: __CollectionIndexType); overload;
        destructor Destroy(); override;
        // Returns a forward iterator over map
        function GetIterator(): _GenHashMapIterator;
        // Returns True if the hash map contains the key
        function Contains(const Key: _HashMapKeyType): Boolean;
        // Returns True if the hash map contains the value
        function ContainsValue(const Value: _HashMapValueType): Boolean;
        // Removes value for the specified key and returns True if there was value for the key
        function Remove(const Key: _HashMapKeyType): Boolean;
        // Calls a delegate for each value stored in the map
        procedure ForEach(Delegate: _HashMapDelegate; Data: Pointer);
        // Returns True if the collection contains no elements
        function IsEmpty(): Boolean;
        // Removes all elements from the collection
        procedure Clear();
        // Values retrieved by key
        property Values[const Key: _HashMapKeyType]: _HashMapValueType read GetValue write SetValue; default;
        // Determines hash function values range which is currently used.
        property Capacity: __CollectionIndexType read FCapacity;
        // Threshold of number of entries to capacity ratio after which capacity doubles. If zero automatic resizing doesn't occur.
        property MaxLoadFactor: Single read FMaxLoadFactor write FMaxLoadFactor;
        // Current number of entries to capacity ratio
        property LoadFactor: Single read GetLoadFactor;
        // Number of entries
        property Size: __CollectionIndexType read FSize write FSize;
    end;

    _GenHashMapIterator = class(__Parent)
        // Advances the iterator to next item and returns True on success or False if no items left
        function GoToNext(): Boolean;
        // Returns current key performing no iterator state changes
        function CurrentKey(): _HashMapKeyType;
        // Returns current value performing no iterator state changes
        function CurrentValue(): _HashMapValueType;
        // Returns True if there is more items
        function HasNext(): Boolean;
        // Advances the iterator to next item and returns it.
        // If no items left nil be returned for nullable collection (dsNullable option is defined) or error generated otherwise.
        function Next(): _HashMapPair;
    end;

    _LinkedListNodePTR = ^_LinkedListNode;
    _LinkedListNode = record
        // List value
        V: _LinkedListValueType;
        // Pointer to next node
        Next: _LinkedListNodePTR;
    end;

    _GenLinkedList = class(__Parent)
    protected
        FFirst, FLast: _LinkedListNodePTR;
        FSize: __CollectionIndexType;

        // Returns list value at the specified position
        procedure SetValue(Index: __CollectionIndexType; const e: _LinkedListValueType); // inline
        // Returns the value assotiated with node p
        function GetNodeValue(p: _LinkedListNodePTR): _LinkedListValueType; // inline
    public
        // Constructs a new empty list
        constructor Create();
        // Frees all nodes and destroys the list
        destructor Destroy(); override;

        { Collection interface }

        // Returns the number of elements in the collection
        function GetSize(): __CollectionIndexType; // inline
        // Returns True if the collection contains no elements
        function IsEmpty(): Boolean; // inline
        // Returns True if the collection contains the specified element
        function Contains(const e: _LinkedListValueType): Boolean;
        // Calls the delegate for each element in the collection
        procedure ForEach(Delegate: _LinkedListDelegate; Data: Pointer);
        {/ Ensures that the collection contains the specified element.
           Returns True if the element was successfully added or False if the collection
           already contains the element and duplicates are not allowed.
           Otherwise the method should raise an error. /}
        function Add(const e: _LinkedListValueType): Boolean; // inline
        {/ Removes the specified element from the collection.
           Returns True if the collection contained the element./}
        function Remove(const e: _LinkedListValueType): Boolean;
        // Frees all nodes makes the list empty
        procedure Clear(); // inline
        // Number of elements
        property Size: __CollectionIndexType read FSize;

        { List interface }

        {/ Returns the element at the specified position in the list.
           Throws an error on invalid index if dsRangeCheck was included in the list options before instantiation. }
        function Get(Index: __CollectionIndexType): _LinkedListValueType; // inline
        {/ Replaces the element at the specified position in the list with the specified element.
           Returns the element previously at the specified position.
           Throws an error on invalid index if dsRangeCheck was included in the list options when instantiation. }
        function Put(Index: __CollectionIndexType; const e: _LinkedListValueType): _LinkedListValueType; // inline
        {/ Inserts the element at the specified position in the list
           Throws an error on invalid index if dsRangeCheck was included in the list options when instantiation. }
        procedure Insert(Index: __CollectionIndexType; const e: _LinkedListValueType);
        {/ Removes the element at the specified position in the list
           Returns the element that was removed from the list. }
        function RemoveBy(Index: __CollectionIndexType): _LinkedListValueType;  // inline
        {/ Returns the index of the first occurrence of the specified element in the list,
           or -1 if the list does not contain the element. }
        function IndexOf(const e: _LinkedListValueType): __CollectionIndexType; // inline
        {/ Returns the index of the last occurrence of the specified element in the list,
           or -1 if the list does not contain the element. }
        function LastIndexOf(const e: _LinkedListValueType): __CollectionIndexType; // inline
        // Values retrieved by index
        property Values[Index: __CollectionIndexType]: _LinkedListValueType read Get write SetValue; default;

        { Linked List interface }

        // Creates and returns a new stand alone node with the same value as p
        function NewNode(const e: _LinkedListValueType): _LinkedListNodePTR; // inline
        // Adds a new node p to the end of the list
        procedure AddNode(p: _LinkedListNodePTR); // inline
        // Inserts a new element e to the beginning of the list
        procedure InsertNodeFirst(const e: _LinkedListValueType); // inline
        // Adds a new element e after the specified node
        procedure InsertNode(Node: _LinkedListNodePTR; const e: _LinkedListValueType); // inline
        // Returns first occured node containing the element
        function GetNode(const e: _LinkedListValueType): _LinkedListNodePTR; // inline
        // Returns note at the specified index
        function GetNodeBy(Index: __CollectionIndexType): _LinkedListNodePTR; // inline

        // Returns a node next to p or nil if p is the last node
        function GetNextNode(p: _LinkedListNodePTR): _LinkedListNodePTR; // inline
        // Returns a node next to p or first node if p is the last node
        function GetNextNodeCyclic(p: _LinkedListNodePTR): _LinkedListNodePTR; // inline

        // Removes p and returns next node
        function RemoveNode(p: _LinkedListNodePTR): _LinkedListNodePTR;
    end;
implementation

end.
