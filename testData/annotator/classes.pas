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
    TOtherClass = record
        f1, f2: Integer;
    end;

implementation

class destructor TOuterClass.Destroy();
begin
    self.myField;
end;

procedure TOuterClass.TInnerClass.innerProc;
var other: TOtherClass;
begin
    with other do begin
        f1 := 0;
        self.myInnerField := 1;
    end;
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