#!/bin/bash

case $1 in
"start"){
  for i in zoo1 zoo2 zoo3
  do
    ssh $i "/apache-zookeeper-3.5.8-bin/bin/zkServer.sh start"
  done
}
;;
"stop"){
  for i in zoo1 zoo2 zoo3
    do
      ssh $i "/apache-zookeeper-3.5.8-bin/bin/zkServer.sh stop"
    done
}
;;
"status"){
  for i in zoo1 zoo2 zoo3
     do
       ssh $i "/apache-zookeeper-3.5.8-bin/bin/zkServer.sh status"
     done
}
;;
esac