def sum(parms:Int*)={
  var result=0
  for(parm <- parms)result+=parm
  result
}

sum(1,2,3)