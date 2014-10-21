#!/bin/csh
gcc -I /usr/include/libxml2/ -L /usr/local/lib/ -o $1 $1.c -lxml2 
