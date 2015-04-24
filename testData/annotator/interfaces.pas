unit structTypes;

interface

type
    Integer = Integer;
    Single = Single;

    IUnknown = interface
    ['{00000000-0000-0000-C000-000000000046}']
        function func(): Single; virtual;
        procedure proc; virtual;
    end;

    TA = class
    private
        procedure IntfGetComponent; virtual;
    public
        function IUnknown.proc = IntfGetComponent;
    end;

implementation

end.