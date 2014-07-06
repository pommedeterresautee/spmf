require(arules)
require(arulesViz)
require(arulesNBMiner)
data("Agrawal")

## mine
param <- NBMinerParameters(Agrawal.db, pi=0.99, theta=0.5, maxlen=5,
                           minlen=1, trim = 0, verb = TRUE, plot=TRUE) 
itemsets_NB <- NBMiner(Agrawal.db, parameter = param, 
                       control = list(verb = TRUE, debug=FALSE))

inspect(head(itemsets_NB))

## remove patterns of length 1 (noise)
i_NB <- itemsets_NB[size(itemsets_NB)>1]
patterns <- Agrawal.pat[size(Agrawal.pat)>1]

## how many found itemsets are subsets of the patterns used in the db?
table(rowSums(is.subset(i_NB,patterns))>0)

## compare with the same number of the most frequent itemsets
itemsets_supp <-  eclat(Agrawal.db, parameter=list(supp=0.001))
i_supp <- itemsets_supp[size(itemsets_supp) >1]
i_supp <- head(sort(i_supp, by = "support"), length(i_NB))
table(rowSums(is.subset(i_supp,patterns))>0)

## mine NB-precise rules
param <- NBMinerParameters(Agrawal.db, pi=0.99, theta=0.5, maxlen=5,
                           rules=TRUE, minlen=1, trim = 0) 
rules_NB <- NBMiner(Agrawal.db, parameter = param, 
                    control = list(verb = TRUE, debug=FALSE))

inspect(head(rules_NB))
