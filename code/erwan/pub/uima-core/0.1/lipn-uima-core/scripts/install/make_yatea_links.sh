#!/bin/bash

#
# EM sept 2010
#

currDir=$(pwd)

function printHelp {
    echo  >/dev/stderr
    echo "This script will create links to YaTeA for convenient use with LIPN UIMA YaTeA annotator" >/dev/stderr
    echo  >/dev/stderr
    echo "Syntax: $0 [options] <path to 'yatea' executable> <path to 'YaTeA' data directory>" >/dev/stderr
    echo "  Options:" >/dev/stderr
    echo "    -h help " >/dev/stderr
    echo "    -d <target dir> create in this dir" >/dev/stderr
    echo  >/dev/stderr
    echo "INFOS : - yatea exec is generally located in a 'bin' dir, e.g. /usr/local/bin" >/dev/stderr
    echo "        - YaTeA dir is generally located in a 'share' dir, e.g. /usr/local/share" >/dev/stderr
    echo  >/dev/stderr
    echo " YaTeA is a Perl module, you can use 'cpan' utility to install it:">/dev/stderr
    echo "install Lingua::YaTeA   (but don't be surprised if that fails...)">/dev/stderr
    echo " For more details about YaTeA install, please refer to the manual.">/dev/stderr

    exit $1
}


function main {

  while getopts 'd:ht:' option ; do # parsing options 
    case $option in
      "d" ) cd "$OPTARG";;
      "h" ) printHelp 0;;
      "?" ) printHelp 1;;
    esac
  done
  shift $(($OPTIND - 1)) 
  if [ $# -ne 2 ]; then
    echo "You must provide path to YaTeA excutable and to YaTeA directory." > /dev/stderr
    printHelp 1
  fi
  yateaExec="$1"
  yateaDir="$2"
  verifFile "$yateaExec"
  verifDir "$yateaDir"
  yateaExec=$(fullPathFile "$yateaExec")
  yateaDir=$(fullPathDir "$yateaDir")
  mkdir bin
  cd bin
  ln -s "$yateaExec"
  cd ..
  ln -s "$yateaDir"
  echo "Done."

}


function fullPathDir {
  dirBak=$(pwd)
  cd "$1"
  pwd
  cd "$dirBak"
}

function fullPathFile {
  dirBak=$(pwd)
  relDir=$(dirname "$1")
  simpleName=$(basename "$1")
  cd "$relDir"
  totalDir=$(pwd)
  echo "$totalDir/$simpleName"
  cd "$dirBak"
}


function verifDir {
  if [ ! -d "$1" ]; then
    echo "Error: dir '$1' does not exist." > /dev/stderr
    cd "$repDebut"
    exit 2
  fi
}


function verifFile {
  if [ ! -e "$1" ]; then
    echo "Error: file '$1' does not exist." > /dev/stderr
    cd "$repDebut"
    exit 2
  fi
}



main "$@"
cd "$currDir"
