SELECT @currentAverage = currentAverage, @currentStandardDeviation = currentStandardDeviation, @currentVariance = currentVariance, @position = position
FROM dimensions
WHERE queryId = @queryId AND position = @@position@@

SELECT @newValue = @position@@position@@

SELECT @newAverage =@currentAverage + (@newValue-@currentAverage)/(@currentRowCount+1)
SELECT @newVariance = (@currentRowCount*@currentVariance+(@newValue-@newAverage)*(@newValue-@currentAverage))/(@currentRowCount+1)
SELECT @newStandardDeviation = sqrt(@newVariance)

UPDATE dimensions 
SET currentAverage = @newAverage, currentStandardDeviation = @newStandardDeviation, currentVariance = @newVariance,
previousAverage = @currentAverage, lastInput = @newValue
WHERE queryId = @queryId AND position = @position
