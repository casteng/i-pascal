{
Built-in identifiers unit.
You may edit this file but any modifications will be discarded after IDE restart.
}
unit $builtins;

interface

type
    string = string;
    Integer = Integer;
    Boolean = Boolean;

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
    function Ord(): Integer;
    function Str(): string;
    function Val(): Integer;
    procedure Write();
    procedure WriteLn();
    procedure Read();
    procedure ReadLn();
    function Length(): Integer;
    procedure SetLength();
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
