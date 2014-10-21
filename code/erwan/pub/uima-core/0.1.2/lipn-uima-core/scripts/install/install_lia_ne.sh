#!/bin/bash

# EM 14/09/10
# may not work !
#

currDir=$(pwd)
url="http://pageperso.lif.univ-mrs.fr/~frederic.bechet/download/lia_ne_v2.2.tgz"
testOnly=0
patch=$(dirname "$0")/patch_lia_ne.sh
extension=".tgz"

function printHelp {
    echo 
    echo "This script will try to install LIA NE, and will probably fail."
    echo "You can at least download the tgz archive before, and provide the location with -a"
    echo 
    echo "Syntax: $0 [options] <lia tagg dir> <CRF++ dir>"
    echo "  Options:"
    echo "    -h help "
    echo "    -a <tgz archive> "
    echo "    -d <target dir> Install in this dir"
    echo "    -t <lia_ne dir> test only"
    echo 
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
      "?" ) printHelp 1   >/dev/stderr;;
    esac
  done
  shift $(($OPTIND - 1)) 
  if [ $# -ne 2 ]; then
    echo "You must provide lia tagg directory and CRF++ directory" >/dev/stderr
    printHelp 1  >/dev/stderr
  fi
  verifDir "$1"
  liaTaggDir=$(fullPathDir "$1")
  export LIA_TAGG="$liaTaggDir"
  verifDir "$2"  
  crfppDir=$(fullPathDir "$2")

  if [ $testOnly -eq 0 ]; then
    if [ -z "$archive" ]; then
      echo -e "\nDownloading LIA NE...\n"
      wget "$url"
      for f in lia_ne*$extension; do
         archive="$f"
      done
    fi
    if [ ! -f $archive ]; then
      echo -e "Can not find archive file: $archive" >/dev/stderr
      exit 2
    fi
    echo -e "\nExtracting archive '$archive'...\n"
    tar xfz "$archive"
    installDir=${archive%$extension}
    if [ ! -d $installDir ]; then
      echo -e "can not find dir: '$installDir' ">/dev/stderr
      exit 3
    fi
    rm -f "$archive"
    export LIA_NE=$(pwd)/"$installDir"

# for the old beta version
#    echo -e "Applying patch for LIA NE ..."
#    if [ -e $patch ]; then
#      $patch "$installDir"
#    else
#      echo "ERROR : could not apply patch '$patch', please check script location" >/dev/stderr
#      exit 4
#    fi

    echo -e "\nCompiling sources...\n"
    cd "$installDir"
    make all
    cd ..
  else
     export LIA_NE="$installDir"
  fi

  echo -e "\nTesting...\n"
  export LIA_TAGG_LANG="french"
  export PATH="$PATH:$crfppDir"
  echo "La Reine Elizabeth II et le président des États-Unis se demandent pourquoi Nicolas Sarkozy est si immature." | "$installDir"/script/lia_ne_tagg_txt

  echo -e "\nHope it worked: should be ok if you see a sentence with some NE tags above.\n"
  cd "$currDir"
}


function fullPathDir {
  dirBak=$(pwd)
  cd "$1"
  pwd
  cd "$dirBak"
}


function verifDir {
  if [ ! -d "$1" ]; then
    echo "Error: directory '$1' does not exist" > /dev/stderr
    cd "$startDir"
    exit 2
  fi
}



main "$@"
