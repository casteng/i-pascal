unit classes;

interface

type
    TOuterClass = class
    strict private
        const
            x = 12;
            y = TOuterClass.x + 23;
        class var
            staticField: Integer;
        var
            myField: Integer;
    public
        type
            TInnerClass = class
            public
                myInnerField: Integer;
                procedure innerProc;
                constructor Create(a:a);
            end;
        procedure outerProc;
        class destructor Destroy();
    end;

implementation

class destructor TOuterClass.Destroy();
begin
end;

procedure TOuterClass.TInnerClass.innerProc;
begin
end;

function m2(const V: TOuterClass; var Operator2: TOuterClass.TInnerClass; out RequiredVarType: TOuterClass): Boolean; forward;

var
    x: TOuterClass;
    y: TOuterClass.TInnerClass;
begin
    TOuterClass.staticField := TOuterClass.y;
    x.outerProc;
    y := TOuterClass.TInnerClass.Create();
    y.innerProc;
    m2(1,2,3);
end.