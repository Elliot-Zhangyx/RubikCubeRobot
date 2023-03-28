#!/bin/bash 
list_alldir(){
for file2 in `ls -A $1`
do
if [ -d "$1/$file2" ];then
#echo "$1/$file2"
list_alldir "$1/$file2"
elif [ -f  "$1/$file2" ];then
 
	if [[ "$1/$file2" == *.java ]] ;then
	
 
	echo "\n" >> out.txt
	echo "$1/$file2" >> out.txt
	echo "\n" >> out.txt
	cat "$1/$file2" >> out.txt
	fi
fi
    done
}
list_alldir ./main
