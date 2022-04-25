DFI Request Processor

Directory layout:
 job
   job files: {domain}_{dfi-push|dfi-pull}_{jobId}.trg
   error files: {domain}_{dfi-push|dfi-pull}_{jobId}.error
 out
   {domain}
     {jobId}
       dfi_out
         dfi_out_{part}.csv
       dfi_in
         dfi_in_{part}.csv
       db_out.csv
       dfi_out.csv 
     
    