IF OBJECT_ID ('dbo.api_trig_@@DEPENDENT_TABLE@@') IS NULL
begin
exec('create trigger api_trig_@@DEPENDENT_TABLE@@ on @@DEPENDENT_TABLE@@ '+
		'for insert ' + 
		'as ' + 
		'update @@META_DB@@..queries set shouldUpdate = 1 ' + 
		'where queryId IN (SELECT DISTINCT queryId FROM @@META_DB@@..query_dependencies WHERE dependentTableName = ''@@DEPENDENT_TABLE@@'')')
end