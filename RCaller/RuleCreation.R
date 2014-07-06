set.seed(1234)

## Generate random data and coerce data to itemMatrix.
m <- matrix(as.integer(runif(100000)>0.8), ncol=20)
dimnames(m) <- list(NULL, paste("item", c(1:20), sep=""))
i <- as(m, "itemMatrix")

## create rules (rhs and lhs cannot share items so I use 
## itemSetdiff here)
rules <- new("rules", lhs=itemSetdiff(i[4:6],i[1:3]), rhs=i[1:3], quality=1)
inspect(rules) 
##  