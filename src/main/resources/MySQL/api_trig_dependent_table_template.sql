CREATE TRIGGER api_trig_@@DEPENDENT_TABLE@@
    after insert on @@DEPENDENT_TABLE@@ for each row
        update @@META_DB@@.queries set shouldUpdate = 1 
		where queryId IN (SELECT DISTINCT queryId FROM @@META_DB@@.query_dependencies WHERE dependentTableName = '@@DEPENDENT_TABLE@@');