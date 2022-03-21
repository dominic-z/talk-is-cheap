#!/bin/sh

A="${1}"

echo "input A=$A"
B=${1}
C=`expr ${B} + 1`
echo "calc res C=$C"



if [ "$A" = "1" ]
then
	content='<h1>Hello, Shell_1!</h1>'
elif [ "$A" = "2" ]
then
	content='<h1>Hello, Shell_2!</h1>'
else
	content='<h1>Hello, Shell_other!</h1>'
fi

echo ${content} > ./shell.html