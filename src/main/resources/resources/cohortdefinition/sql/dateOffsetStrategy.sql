-- date offset strategy

SELECT
    event_id,
    person_id,
    CASE
        WHEN @offset IS NOT NULL
            THEN
                CASE
                    WHEN DATEADD(@offsetUnit, @offsetUnitValue, @dateField) > op_end_date THEN op_end_date
                    ELSE DATEADD(@offsetUnit, @offsetUnitValue, @dateField)
                END
        ELSE
            CASE
                WHEN DATEADD(day, @offset, @dateField) > op_end_date THEN op_end_date
                ELSE DATEADD(day, @offset, @dateField)
            END
    END AS end_date
INTO #strategy_ends
FROM @eventTable;
