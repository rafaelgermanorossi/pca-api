when '@@updateProcedure@@' then call @@updateProcedure@@(@PcurrentRowCount, @PcurrentIdentityMaxValue);
SET new.currentRowCount = @PcurrentRowCount, new.currentIdentityMaxValue = @PcurrentIdentityMaxValue, new.shouldUpdate = 0;
