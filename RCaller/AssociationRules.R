# Load required libraries
require(XLConnect)  # Excel files
#require(scales) # Help to manage Dates


pathExcelFile <- "AssociationRules.xlsx"

data <- readWorksheetFromFile(file = pathExcelFile, header = TRUE, sheet = 1)

#Simplify Debit/Credit fields
data$Debit <- ifelse(data$Debit==0,F,T)
data$Credit <- ifelse(data$Credit==0,F,T)

#Not required columns to delete
toDelete <- c("Débit","Crédit", "Solde", "Chrono", "EcritureNum", "COUNT1")
data <- data[,!(names(data) %in% toDelete)]
col_names <- names(data)
data[,col_names] <- lapply(data[,col_names] , factor)

# remove columns with the same value everywhere
namesToKeep <- sapply(data,function(x) length(unique(x)))
data <- subset(data, select=namesToKeep>1)


require(arules)
require(arulesViz)

#includes a Filter on the rules -> should say Debit = False
rules = apriori(data, 
                parameter=list(support=0.1, confidence=0.8, minlen=2), 
                appearance = list(rhs=c("Debit=FALSE"), default="lhs"), 
                control = list(verbose=F))

# find redundant rules, keep the more complex one -> includes as many columns as possible
rules.sorted <- sort(rules, by="lift")
subset.matrix <- is.subset(rules.sorted, rules.sorted)
subset.matrix[lower.tri(subset.matrix, diag=T)] <- NA
redundant <- colSums(subset.matrix, na.rm=T) >= 1
which(redundant)
# remove redundant rules
rules.pruned <- rules.sorted[!redundant]


#display 10 rules
inspect(head(sort(rules.pruned, by="lift"),10))
#Graph
plot(rules.pruned, measure=c("support","lift"), shading="confidence")
plot(rules.pruned, shading="order", control=list(main ="Two-key plot"))
plot(rules.pruned, method="paracoord", control=list(reorder=TRUE))
plot(rules.pruned, method="graph", control=list(type="items"))

#require(arulesNBMiner)
# conversion to class transaction
#data <- as(data, "transactions") <- seams to fail ? Look at summary(data), too many columns

#convert rules to Dataframe to save them in a file
#as(rules_1, "data.frame");