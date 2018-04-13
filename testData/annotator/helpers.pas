unit helpers;

interface

type
    TA = class
    end;

    THelper = class helper for TA
    end;
    TAHelper = class helper(THelper) for TA
        Name: string;
        class function Func: TA;
        constructor Create(a, b: TA);
    end;

    TVec = packed record
        x, Y: Single;
    end;

    TVecHelper = record helper for TVec
        class function v: TVec;
        constructor Create();
    end;

implementation

class function TAHelper.Func: TA;
begin
end;

constructor TAHelper.Create(a, b: TA);
begin
end;

class function TVecHelper.v: TVec;
begin
end;

constructor TVecHelper.Create();
begin
end;

end.