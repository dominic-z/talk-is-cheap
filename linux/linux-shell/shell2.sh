for skill in Ada Coffe Action Java; do
    echo "I am good at ${skill}Script"
done

# for file in $(ls /etc); do
#     echo ${file}
# done

myUrl="https://www.runoob.com"
unset myUrl
echo $myUrl


str='this is a string'
echo ${str}

your_name='runoob'
str="Hello, I know you are \"$your_name\"! \n"
echo -e $str


your_name="runoob"
# 使用双引号拼接
greeting="hello, "$your_name" !"
greeting_1="hello, ${your_name} !"
echo $greeting  $greeting_1
# 使用单引号拼接
greeting_2='hello, '$your_name' !'
greeting_3='hello, ${your_name} !'
echo $greeting_2  $greeting_3

echo '\n'
array_name=("value0" "value1" "value2" "value3")
array_name[3]="value4_${str}"
echo ${array_name[@]}
echo ${array_name[1]}
echo ${array_name[3]}