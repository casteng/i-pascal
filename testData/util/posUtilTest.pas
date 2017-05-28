unit posUtilTest;

interface

type
  TClass1 = class()
  end;
  TClass2 = class
  protected
  end;
  TClass3 = class()
    private
    public
    private
  end;
  TClass4 = class()
    field: TClass1;
  end;
  TClass5 = class
    field: TClass1;
    procedure proc1;
    property prop: TClass1 read field;
  end;
  TClass6 = class()
  private
    field: TClass1;
    procedure proc1;
  public
    constructor Create();
  end;
  TClass7 = class()
  protected
    field: TClass1;
    property prop: TClass1 read field;
  public
    property prop2: TClass1 read field;
  end;
  TClass8 = class()
  published
    destructor Destroy(); override;
    property prop2: TClass1 read field;
  end;

implementation

end.
