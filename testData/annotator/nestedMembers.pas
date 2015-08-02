unit nestedMembers;

interface

type
    TTest1 = class
    strict private
        const
            c = 23;
        type
            TInner = class
            public
                myInnerField: Integer;
                var
                    v: Integer;
            end;
    end;

    TTest2 = record
        private var
            v: Integer;
        type
            T2 = Integer;
    end;

    TTest3 = class
    type
        T3 = Integer;
    const
        y = 23;
    end;

implementation

end.