
num1=100
num2=100
if test $[num1] -eq $[num2]
then
    echo '两个数相等！'
else
    echo '两个数不相等！'
fi

if [ ${num1} -eq ${num2} ]
then
    echo '两个数相等！'
else
    echo '两个数不相等！'
fi


a=5
b=6

result=$[a+b] # 注意等号两边不能有空格
echo "result 为： $result"

result=`expr $a + $b ` # 注意等号两边不能有空格
echo "result 为： $result"