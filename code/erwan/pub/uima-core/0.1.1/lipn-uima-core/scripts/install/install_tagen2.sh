#!/bin/bash

# EM 14/09/10
# may not work !
#

currDir=$(pwd)
url="http://www-lipn.univ-paris13.fr/~moreau/tagen2.tgz"
testOnly=0

function printHelp {
    echo  >/dev/stderr
    echo "This script will try to install Tagen2, and will probably fail." >/dev/stderr
    echo "You should at least download Tagen2 before, and provide the location with -u" >/dev/stderr
    echo  >/dev/stderr
    echo "Syntax: $0 [options] <unitex dir>" >/dev/stderr
    echo "  Options:" >/dev/stderr
    echo "    -h help " >/dev/stderr
    echo "    -e <tagen tgz> tagen2 .tgz file" >/dev/stderr
    echo "    -d <target dir> Install in this dir" >/dev/stderr
    echo "    -t <tagen2 dir> test only" >/dev/stderr
    echo  >/dev/stderr
    exit $1
}


function main {

  while getopts 'd:t:e:h' option ; do # parsing options 
    case $option in
      "e" ) tagenTgz="$OPTARG";;
      "d" ) cd "$OPTARG";;
      "t" ) tagenDir="$OPTARG"
            testOnly=1;;
      "h" ) printHelp 0;;
      "?" ) printHelp 1;;
    esac
  done
  shift $(($OPTIND - 1)) 
  if [ $# -ne 1 ]; then
    echo "Error: you must provide path to Unitex directory.">/dev/stderr
    printHelp 1
  fi
  unitexDir=$(fullPathDir "$1")
  verifDir "$unitexDir"

  if [ $testOnly -eq 0 ]; then

    #  tagen2
    if [ -z "$tagenTgz" ]; then
      echo -e "\nDownloading Tagen...\n"
      wget "$url"
      tagenTgz=$(basename "$url")
    fi
    if [ ! -f $tagenTgz ]; then
      echo -e "Can not find Tagen tgz file: $tagenTgz" >/dev/stderr
      exit 2
    fi
    echo -e "\nExtracting Tagen...\n"
    tar xfz "$tagenTgz"
    tagenDir=${tagenTgz%*.tgz}
    if [ ! -d "$tagenDir" ]; then
      echo -e "can not find dir: $tagenDir ">/dev/stderr
      exit 3
    fi
    cd "$tagenDir"
    ln -s "$unitexDir" "unitex"
    cd ..
    rm -f "$tagenTgz"
    echo -e "\nHope it's ok for TagEN.\n"
  fi

  echo -e "\nTesting...\n"
  input=$(mktemp)
  output=$(mktemp)
  echo "Queen Elizabeth II and the president of the USA wonder why Nicolas Sarkozy is so immature." > "$input"
  "$tagenDir"/tagen -u "$unitexDir" -t "$tagenDir" -c UTF-8 eng "$input" "$output"
  echo -e "\nTest result:\n"
  cat "$output"
  echo -e "\nHope that worked: you should see a sentence containing some NE tags above.\n"
  rm -f "$input" "$output"
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
cd "$currDir"
