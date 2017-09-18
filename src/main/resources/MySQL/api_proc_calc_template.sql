SELECT currentAverage, currentStandardDeviation, currentVariance, position
INTO PcurrentAverage, PcurrentStandardDeviation, PcurrentVariance, Pposition
FROM dimensions
WHERE queryId = PqueryId AND position = @@position@@;

SET PnewValue := Pposition@@position@@;

SET PnewAverage := PcurrentAverage + (PnewValue-PcurrentAverage)/(PcurrentRowCount+1);
SET PnewVariance := (PcurrentRowCount*PcurrentVariance+(PnewValue-PnewAverage)*(PnewValue-PcurrentAverage))/(PcurrentRowCount+1);
SET PnewStandardDeviation := sqrt(PnewVariance);

UPDATE dimensions 
SET currentAverage = PnewAverage, currentStandardDeviation = PnewStandardDeviation, currentVariance = PnewVariance,
previousAverage = PcurrentAverage, lastInput = PnewValue
WHERE queryId = PqueryId AND position = Pposition;
