#!/bin/bash

# EM 14/09/10
# may not work !
#

currDir=$(pwd)
url="http://sourceforge.net/projects/crfpp/files/crfpp/0.54/CRF%2B%2B-0.54.tar.gz/download"
testOnly=0
extension=".tar.gz"

function printHelp {
    echo  
    echo "This script will try to install CRF++, and will probably fail." 
    echo "You can at least download the tar.gz archive before, and provide the location with -a" 
    echo  
    echo "Syntax: $0 [options]" 
    echo "  Options:" 
    echo "    -h help " 
    echo "    -a <tar gz archive> " 
    echo "    -d <target dir> Install in this dir" 
    echo  
    exit $1
}


function main {

  while getopts 'a:d:ht:' option ; do # parsing options 
    case $option in
      "a" ) archive="$OPTARG";;
      "d" ) cd "$OPTARG";;
      "h" ) printHelp 0;;
      "t" ) installDir="$OPTARG"
            testOnly=1;;
      "?" ) printHelp 1 >/dev/stderr;;
    esac
  done
  shift $(($OPTIND - 1)) 

  if [ $testOnly -eq 0 ]; then
    if [ -z "$archive" ]; then
      echo -e "\nDownloading CRF++...\n"
      wget "$url"
      if [ -f download ]; then
        mv download "CRF++$extension"
      fi
      for f in CRF*$extension; do
         archive="$f"
      done
    fi
    if [ ! -f $archive ]; then
      echo -e "Can not find archive file: $archive" >/dev/stderr
      exit 2
    fi
    echo -e "\nExtracting archive '$archive'...\n"
    tar xfz "$archive"
      for f in CRF*; do
         if [ -d "$f" ]; then
           installDir="$f"
         fi
      done
    if [ ! -d $installDir ]; then
      echo -e "can not find dir: '$installDir' ">/dev/stderr
      exit 3
    fi
    rm -f "$archive"
    cd "$installDir"
    echo -e "\nCompiling sources...\n"
    ./configure --prefix $(pwd)
    make
    make install
    cd ..
  fi

  cd "$currDir"
}




main "$@"
