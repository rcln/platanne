#!/bin/bash

# EM 14/09/10
# may not work !
#

currDir=$(pwd)
urlUnitex="http://www-igm.univ-mlv.fr/~unitex/Unitex2.1.zip"
#urlUnitex="http://www-igm.univ-mlv.fr/~unitex/zips/Unitex2.1beta.zip"
#urlUnitex="http://www-igm.univ-mlv.fr/~unitex/Unitex2.0.zip"
testOnly=0
patch=$(dirname "$0")/patch_unitex2.0.sh

function printHelp {
    echo  >/dev/stderr
    echo "This script will try to install Unitex, and will probably fail." >/dev/stderr
    echo "You should at least download Unitex before, and provide the location with -u" >/dev/stderr
    echo  >/dev/stderr
    echo "Syntax: $0 [options]" >/dev/stderr
    echo "  Options:" >/dev/stderr
    echo "    -h help " >/dev/stderr
    echo "    -u <unitex zip> Unitex  .zip file" >/dev/stderr
    echo "    -d <target dir> Install in this dir" >/dev/stderr
    echo  >/dev/stderr
    exit $1
}


function main {

  while getopts 'u:d:t:e:h' option ; do # parsing options 
    case $option in
      "u" ) unitexZip="$OPTARG";;
      "d" ) cd "$OPTARG";;
      "h" ) printHelp 0;;
      "?" ) printHelp 1;;
    esac
  done
  shift $(($OPTIND - 1)) 

  if [ $testOnly -eq 0 ]; then
    # unitex
    if [ -z "$unitexZip" ]; then
      echo -e "\nDownloading Unitex...\n"
      wget "$urlUnitex"
      unitexZip=$(basename "$urlUnitex")
    fi
    if [ ! -f $unitexZip ]; then
      echo -e "Can not find Unitex zip file: $unitexZip" >/dev/stderr
      exit 2
    fi
    echo -e "\nUnzipping Unitex...\n"
    unzip -o -q "$unitexZip"
    unitexDir=${unitexZip%*.zip}
    if [ ! -d $unitexDir ]; then
      echo -e "can not find dir: $unitexDir ">/dev/stderr
      exit 3
    fi
    rm -f "$unitexZip"

    noPatch=0
    version=$(echo "$unitexDir" | grep "2.0")
    if [ -z "$version" ]; then
      noPatch=1
    fi

    if [ $noPatch -eq 0 ]; then
      echo -e "\nApplying patch for Unitex2.0 ...\n"
      if [ -e $patch ]; then
        $patch "$unitexDir"
      else
        echo "ERROR : could not apply patch '$patch', please check script location" >/dev/stderr
        exit 4
      fi
    else
      echo -e "\nDectecting version >=2.1 -> fine, no patch.\n"
    fi


    cd "$unitexDir/Src/C++/build"
    echo -e "\nCompiling Unitex sources...\n"
    make install
    cd ../../../..
    echo -e "\nHope it's ok for Unitex.\n"
  fi

}

main "$@"
cd "$currDir"
