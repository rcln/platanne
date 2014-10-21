#!/bin/bash

# EM sept 2010
# patch for compil errors in Unitex 2.0 
# => won't work in further versions (if any), but no need since it's already corrected 2.1beta
# (but currently tagen does not work with unitex 2.1 !)

targetDir="Src/C++"
targets1="utils.h Elag.cpp"
old1="basename"
new1="basename_patched"
target2="getopt.c"
old2="strchr(options, optchar)"
new2="strchr((char *) options, optchar)"

if [ $# -ne 1 ]; then
  echo >/dev/stderr
  echo "Syntax: $0 <path to Unitex 2.0>" >/dev/stderr
  echo "This patch will correct compil errors in Unitex 2.0" >/dev/stderr
  echo >/dev/stderr
  exit 1
fi
dir="$1"
for f in $targets1; do
  cp "$dir/$targetDir/$f" "$dir/$targetDir/$f.bak"
  sed s/$old1/$new1/g <"$dir/$targetDir/$f.bak" >"$dir/$targetDir/$f"
done
cp "$dir/$targetDir/$target2" "$dir/$targetDir/$target2.bak"
  sed s/$old2/$new2/g <"$dir/$targetDir/$target2.bak" >"$dir/$targetDir/$target2"


exit 0


