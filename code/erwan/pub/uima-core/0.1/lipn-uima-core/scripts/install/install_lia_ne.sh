#!/bin/bash

# EM 14/09/10
# may not work !
#

currDir=$(pwd)
url="http://pageperso.lif.univ-mrs.fr/~frederic.bechet/download/lia_ne.tar.bz2"
testOnly=0
patch=$(dirname "$0")/patch_lia_ne.sh

function printHelp {
    echo  >/dev/stderr
    echo "This script will try to install LIA NE, and will probably fail." >/dev/stderr
    echo "You can at least download the tar.bz2 archive before, and provide the location with -a" >/dev/stderr
    echo  >/dev/stderr
    echo "Syntax: $0 [options] <lia tagg dir>" >/dev/stderr
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
  if [ $# -ne 1 ]; then
    echo "You must provide lia tagg directory" >/dev/stderr
    printHelp 1
  fi
  export LIA_TAGG="$1"

  if [ $testOnly -eq 0 ]; then
    if [ -z "$archive" ]; then
      echo -e "\nDownloading LIA NE...\n"
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
    export LIA_NE=$(pwd)/"$installDir"


    echo -e "Applying patch for LIA NE ..."
    if [ -e $patch ]; then
      $patch "$installDir"
    else
      echo "ERROR : could not apply patch '$patch', please check script location" >/dev/stderr
      exit 4
    fi

    echo -e "\nCompiling sources...\n"
    cd "$installDir"/CRF++-0.41
    ./configure
    make
    cd ..
    make all
    cd ..
  else
     export LIA_NE="$installDir"
  fi

  echo -e "\nTesting...\n"
  export LIA_TAGG_LANG="english"
  echo "Queen Elizabeth II and the president of the USA wonder why Nicolas Sarkozy is so immature." | "$installDir"/script/ne_tagg.csh

  echo -e "\nHope it worked: should be ok if you see a sentence with some NE tags above.\n"
  cd "$currDir"
}




main "$@"
