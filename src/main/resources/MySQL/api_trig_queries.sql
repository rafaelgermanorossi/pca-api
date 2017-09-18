create trigger api_trig_queries before update on queries for each row 
begin 
	IF (new.shouldUpdate <> old.shouldUpdate AND new.shouldUpdate = 1 AND new.updateMethod = 'trigger') then 
		 case new.updateProcedure 
		  	when null then begin end; 
		  -- @@replace@@ 
		 end case; 
	end if; 
end; 