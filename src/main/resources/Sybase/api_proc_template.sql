create procedure api_proc_@@queryId@@
as 

SET arithabort numeric_truncation off

DECLARE @queryId VARCHAR(100)
SELECT @queryId = '@@queryId@@'

DECLARE @currentAverage real
DECLARE @currentStandardDeviation real
DECLARE @currentVariance real
DECLARE @currentRowCount real
DECLARE @currentIdentityMaxValue VARCHAR(100)

DECLARE @newAverage real
DECLARE @newStandardDeviation real
DECLARE @newVariance real
DECLARE @newValue real

declare @position int

@@DECLARE@@

SELECT @currentRowCount = currentRowCount, @currentIdentityMaxValue = currentIdentityMaxValue FROM queries WHERE queryId = @queryId

delete from api_aux_table where queryId = @queryId

insert into api_aux_table
SELECT @queryId, convert(VARCHAR, @@identityColumn@@)
			 	FROM @@DATA_SCHEMA@@..@@viewName@@
			  WHERE @@identityColumn@@ > convert(@@identityColumnType@@, @currentIdentityMaxValue)

DECLARE NEWOBSCUR CURSOR FOR 
	SELECT @@DIMENSIONS@@, identityColumn
		FROM 
			api_aux_table I
		join 
		@@DATA_SCHEMA@@..@@viewName@@ V ON I.queryId = @queryId and convert(@@identityColumnType@@, I.identityColumn) = V.@@identityColumn@@
	ORDER BY convert(@@identityColumnType@@, I.identityColumn)
	
FOR READ ONLY
OPEN NEWOBSCUR
FETCH NEWOBSCUR INTO @@DIMENSIONS_VARIABLES@@, @currentIdentityMaxValue
while @@sqlstatus = 0 
begin
	
BEGIN TRAN

	@@CALC@@
	
update covariances 
set value = (@currentRowCount/(@currentRowCount+1))*(value + (d1.lastInput - d1.previousAverage)*(d2.lastInput - d2.previousAverage)/(@currentRowCount+1))
FROM covariances c
	join dimensions d1 on c.queryId = d1.queryId and c.d1Pos = d1.position
	join dimensions d2 on c.queryId = d2.queryId and c.d2Pos = d2.position
where c.queryId = @queryId
	
SELECT @currentRowCount = @currentRowCount+1

UPDATE queries SET currentRowCount = @currentRowCount, currentIdentityMaxValue = @currentIdentityMaxValue WHERE queryId = @queryId

COMMIT TRAN
FETCH NEWOBSCUR INTO @@DIMENSIONS_VARIABLES@@, @currentIdentityMaxValue

END			
close NEWOBSCUR 
deallocate cursor NEWOBSCUR 

UPDATE queries SET shouldUpdate = 0 WHERE queryId = @queryId

SET arithabort numeric_truncation on