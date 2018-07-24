unit recordConst;

interface

type
    Integer = Integer;
    string = string;

    TVec = packed record
        Name: string;
        Value: Integer;
    end;

const
    crNone = 0;
    CArr: array[0..22] of TVec = (
    (Value: crNone; Name: 'crNone'),
    (Value: 1;      Name: 'crArrow'),
    (Value: 2;      Name: 'crCross'),
    (Value: 3;      Name: 'crIBeam'),
    { Dead cursors }
    (Value: $FFFF;  Name: 'crSize'));

implementation

end.