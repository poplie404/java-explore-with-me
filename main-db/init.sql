DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'events' 
        AND column_name = 'annotation' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE events ALTER COLUMN annotation TYPE text USING annotation::text;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'events' 
        AND column_name = 'description' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE events ALTER COLUMN description TYPE text USING description::text;
    END IF;
END $$;

