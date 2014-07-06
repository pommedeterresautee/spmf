library(rJava)
.jinit( parameters="-Xmx2048m")


.jaddClassPath('../out/artifacts/spmf_jar/spmf.jar')

#.jcall("com.taj.caller.AnalyseFP","V","main",.jarray(list(), "java/lang/String"))

 .jcall("com.taj.caller.AnalyseFP","V","doIt","/home/geantvert/workspace/spmf/RCaller/Data/arfff", "/home/geantvert/workspace/spmf/RCaller/Data/result", 0.3, 0.3)

