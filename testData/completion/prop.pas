unit prop;

interface

type
    TProp = class
    public
        X, Y: Integer;
    end;

    TFoo = class
    private
        FFaa: TProp;
        property Faa: TProp read FFaa;
    end;

    TFooChild = class(TFoo)
    public
        property Faa;
    end;

implementation

{$R *.dfm}

procedure a();
var
    T: TFooChild;
begin
    T.Faa.<caret>
end;

end.
