create trigger api_trig_queries on queries for update
as 

DECLARE @queryId VARCHAR(100)
DECLARE @updateProcedure VARCHAR(100)

	declare CUR cursor 
	for select inserted.queryId, inserted.updateProcedure 
			FROM inserted, deleted 
			WHERE inserted.queryId = deleted.queryId AND 
				  inserted.shouldUpdate <> deleted.shouldUpdate AND 
				  inserted.shouldUpdate = 1 AND
				  inserted.updateMethod = 'trigger'
	for read only 
	open CUR 
	fetch CUR into @queryId, @updateProcedure
 
	while @@sqlstatus = 0 
	begin 
 
		EXEC sp_exec_SQL @updateProcedure, 'api_trig_queries'
 
	  	fetch CUR into @queryId, @updateProcedure
	end 
	close CUR 
	deallocate cursor CUR 
