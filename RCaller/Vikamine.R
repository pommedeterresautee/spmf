require(subgroup)
.jinit( parameters="-Xmx8000m")
.jaddClassPath("/home/geantvert/R/x86_64-pc-linux-gnu-library/3.0/subgroup/java/subgroup.jar")

require(XLConnect) 


pathExcelFile <- "Data/AssociationRules.xlsx"

data <- readWorksheetFromFile(file = pathExcelFile, header = TRUE, sheet = 1)

#Simplify Debit/Credit fields
data$Debit <- ifelse(data$Debit==0,F,T)
data$Credit <- ifelse(data$Credit==0,F,T)
data$MontantDevise <- ifelse(data$MontantDevise==0,F,T)

#Not required columns to delete
toDelete <- c("Débit","Crédit", "Solde", "Chrono", "EcritureNum", "COUNT1")
data <- data[,!(names(data) %in% toDelete)]
col_names <- names(data)
data[,col_names] <- lapply(data[,col_names] , factor)

# remove columns with the same value everywhere
namesToKeep <- sapply(data,function(x) length(unique(x)))
data <- subset(data, select=namesToKeep>1)

result1 <- DiscoverSubgroups(data, as.target("PieceRef", "Special"),new("SDTaskConfig", method="bsd", maxlen = 2, k = 100, minsize = 20, qf = "wracc", postfilter = "min-improve-set"))

p1 <- ToDataFrame(result1, ndigits = 2)

p1 <- p1[c(1:15),]

View(p1)


.jinit( parameters="-Xmx8000m")
# Read CSV
SCC324_319472775FEC20121231 <- read.delim("~/FEC/SCC324_319472775FEC20121231.csv", dec=",")

#replace values
SCC324_319472775FEC20121231$Debit <- ifelse(SCC324_319472775FEC20121231$Debit==0,F,T)
SCC324_319472775FEC20121231$Credit <- ifelse(SCC324_319472775FEC20121231$Credit==0,F,T)


#delete some columns
toDelete <- c("Débit","Crédit", "Solde", "Chrono", "COUNT1", "CompteLib", "EcritureLib", "JournalPrincipal", "JournalLib", "CompAuxLib")
SCC324_319472775FEC20121231 <- SCC324_319472775FEC20121231[,!(names(SCC324_319472775FEC20121231) %in% toDelete)]

#remove univalue columns
namesToKeep <- sapply(SCC324_319472775FEC20121231,function(x) length(unique(x)))
SCC324_319472775FEC20121231 <- subset(SCC324_319472775FEC20121231, select=namesToKeep > 1)

#transform each column to a Factor
col_names <- names(SCC324_319472775FEC20121231)
SCC324_319472775FEC20121231[,col_names] <- lapply(SCC324_319472775FEC20121231[,col_names] , factor)

# remove spaces from factors and String columns
SCC324_319472775FEC20121231 <- as.data.frame(lapply(SCC324_319472775FEC20121231,function(x) if(is.character(x)|is.factor(x)) gsub(" ","",x) else x))

#write file to the HD
write.arff(SCC324_319472775FEC20121231, "/home/geantvert/toto.arff")

task <- CreateSDTask("/home/geantvert/toto.arff", as.target("C", "401110"),new("SDTaskConfig", method="bsd", maxlen = 2, k = 100, minsize = 20, qf = "wracc", postfilter = "min-improve-set"))


require(foreign)
data <- read.arff("/home/geantvert/toto.arff")

library(doParallel)
registerDoParallel(cores=7)
y <- data$F
vars <- c(1:3)
require(bigrf)
forest <- bigrfc(data, y, ntree=30L, varselect=vars)
