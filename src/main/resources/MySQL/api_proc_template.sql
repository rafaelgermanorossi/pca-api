create procedure api_proc_@@queryId@@(out PcurrentRowCount double, out PcurrentIdentityMaxValue VARCHAR(100))
begin
declare done boolean;
DECLARE PqueryId VARCHAR(100);
DECLARE PcurrentAverage double;
DECLARE PcurrentStandardDeviation double;
DECLARE PcurrentVariance double;

DECLARE PnewAverage double;
DECLARE PnewStandardDeviation double;
DECLARE PnewVariance double;
DECLARE PnewValue double;

declare Pposition int;

@@DECLARE@@

DECLARE NEWOBSCUR CURSOR FOR 
	SELECT @@DIMENSIONS@@, identityColumn
		FROM 
			api_aux_table I
		join 
		@@DATA_SCHEMA@@.@@viewName@@ V ON I.queryId = PqueryId and CAST(I.identityColumn AS @@identityColumnType@@) = V.@@identityColumn@@
	ORDER BY CAST(I.identityColumn AS @@identityColumnType@@);
	
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;


SET PqueryId := '@@queryId@@';
SELECT currentRowCount, currentIdentityMaxValue into PcurrentRowCount, PcurrentIdentityMaxValue FROM queries WHERE queryId = PqueryId;

delete from api_aux_table where queryId = PqueryId;

insert into api_aux_table
SELECT PqueryId, CAST(@@identityColumn@@ AS CHAR)
			 	FROM @@DATA_SCHEMA@@.@@viewName@@
			  WHERE @@identityColumn@@ > CAST(PcurrentIdentityMaxValue AS @@identityColumnType@@);

OPEN NEWOBSCUR;

read_loop: LOOP
    FETCH NEWOBSCUR INTO @@DIMENSIONS_VARIABLES@@, PcurrentIdentityMaxValue;
    IF done THEN
      LEAVE read_loop;
    END IF;

	@@CALC@@
	
update covariances C
	join dimensions d1 on c.queryId = d1.queryId and c.d1Pos = d1.position
	join dimensions d2 on c.queryId = d2.queryId and c.d2Pos = d2.position
	set value = (PcurrentRowCount/(PcurrentRowCount+1))*(value + (d1.lastInput - d1.previousAverage)*(d2.lastInput - d2.previousAverage)/(PcurrentRowCount+1))
where c.queryId = PqueryId;
	
SET PcurrentRowCount := PcurrentRowCount+1;
-- @@remove@@ update queries SET currentRowCount = PcurrentRowCount, currentIdentityMaxValue = PcurrentIdentityMaxValue, shouldUpdate = 0 where queryId = PqueryId;
  END LOOP;
  
close NEWOBSCUR;  

end