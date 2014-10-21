#!/bin/bash

# EM 14/09/10
# may not work !
#

currDir=$(pwd)
url="http://pageperso.lif.univ-mrs.fr/~frederic.bechet/download/lia_tagg.dec06.tar.bz2"
testOnly=0

function printHelp {
    echo  >/dev/stderr
    echo "This script will try to install LIA TAGG, and will probably fail." >/dev/stderr
    echo "You can at least download the tar.bz2 archive before, and provide the location with -a" >/dev/stderr
    echo  >/dev/stderr
    echo "Syntax: $0 [options]" >/dev/stderr
    echo "  Options:" >/dev/stderr
    echo "    -h help " >/dev/stderr
    echo "    -a <tar bz2 archive> " >/dev/stderr
    echo "    -d <target dir> Install in this dir" >/dev/stderr
    echo "    -t <lia_tagg dir> test only" >/dev/stderr
    echo  >/dev/stderr
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
      "?" ) printHelp 1;;
    esac
  done
  shift $(($OPTIND - 1)) 

  if [ $testOnly -eq 0 ]; then
    if [ -z "$archive" ]; then
      echo -e "\nDownloading LIA TAGG...\n"
      wget "$url"
      archive=$(basename "$url")
    fi
    if [ ! -f $archive ]; then
      echo -e "Can not find archive file: $archive" >/dev/stderr
      exit 2
    fi
    echo -e "\nExtracting archive '$archive'...\n"
    tar xfj "$archive"
    installDir=${archive%%.*}
    if [ ! -d $installDir ]; then
      echo -e "can not find dir: '$installDir' ">/dev/stderr
      exit 3
    fi
    rm -f "$archive"
    export LIA_TAGG=$(pwd)/"$installDir"
    cd "$installDir"
    echo -e "\nCompiling sources...\n"
    make all
    make ressource.all
    cd ..
  else
     export LIA_TAGG="$installDir"
  fi

  echo -e "Testing..."
  export LIA_TAGG_LANG="english"
  
  echo "This example sentence is very simple." | "$installDir"/script/lia_clean | "$installDir"/script/lia_tagg+lemm

  echo -e "\nHope it worked: should be ok if you see something which looks like a parse.\n"
  cd "$currDir"
}




main "$@"
