#!/bin/bash

#
# EM sept 2010
# run all unsure install scripts
#
# may not work !
#

currDir=$(pwd)

installDir="tools"
noYatea=0
testOnly=0

function printHelp {
    echo  >/dev/stderr
    echo "This script will try to install all programs used by the lipn uima platform" >/dev/stderr
    echo " in a directory named '$installDir'" >/dev/stderr
    echo "YaTeA must have been installed before running this script (or use option -n" >/dev/stderr
    echo "if you want all tools but YaTeA).">/dev/stderr
    echo  >/dev/stderr
    echo "Syntax: $0 [options] <path to 'yatea' executable> <path to 'YaTeA' data directory>" >/dev/stderr
    echo "  Options:" >/dev/stderr
    echo "    -h help " >/dev/stderr
    echo "    -d <target dir> Install in this dir (dir '$installDir' will be created there)" >/dev/stderr
    echo "    -t <tools dir> test only" >/dev/stderr
    echo "    -n don't want yatea stuff (no mandatory args needed in this case)">/dev/stderr
    echo  >/dev/stderr
    echo "CAUTION: this script is really unsure, probably that most tools won't be ">/dev/stderr
    echo "         installed. But it is not unsafe, nothing is deleted.">/dev/stderr
    echo  >/dev/stderr
    echo "-> All sub-scripts named 'install_xxx' must be in the same dir than this script"
    echo  >/dev/stderr
    echo "INFOS : - yatea exec is generally located in a 'bin' dir, e.g. /usr/local/bin" >/dev/stderr
    echo "        - YaTeA dir is generally located in a 'share' dir, e.g. /usr/local/share" >/dev/stderr
    echo  >/dev/stderr
    echo " YaTeA is a Perl module, you can use 'cpan' utility to install it:">/dev/stderr
    echo "install Lingua::YaTeA   (but don't be surprised if that fails...)">/dev/stderr
    echo " For more details about YaTeA install, please refer to the manual.">/dev/stderr 
    echo  >/dev/stderr
    echo "This script may take a few minutes."
    exit $1
}


function main {

  thisScriptDir=$(dirname "$0")
  thisScriptDir=$(fullPathDir "$thisScriptDir")

  while getopts 'd:ht:n' option ; do # parsing options 
    case $option in
      "d" ) cd "$OPTARG";;
      "h" ) printHelp 0;;
      "t" ) installDir="$OPTARG"
            testOnly=1;;
      "n" ) noYatea=1;;
      "?" ) printHelp 1;;
    esac
  done
  shift $(($OPTIND - 1)) 
  if [ $# -ne 2 ] && [ $noYatea -eq 0 ]; then
    echo "ERROR: you must provide path to YaTeA executable and to YaTeA directory." > /dev/stderr
    printHelp 1
  fi
  yateaExec="$1"
  yateaDir="$2"

  if [ $testOnly -eq 0 ]; then
    mkdir "$installDir"
    cd "$installDir"

    echo "####  INSTALLING ALL ..."
    "$thisScriptDir"/install_tree_tagger.sh
    "$thisScriptDir"/install_unitex.sh
    for f in Unitex*; do
      unitexDir="$f"
    done
    "$thisScriptDir"/install_tagen2.sh "$unitexDir"
    for f in tagen2*; do
      tagenDir="$f"
    done
    ln -s "$tagenDir" "tagen2"
    "$thisScriptDir"/install_lia_tagg.sh
    "$thisScriptDir"/install_lia_ne.sh lia_tagg
    if [ $noYatea -eq 0 ]; then
      "$thisScriptDir"/make_yatea_links.sh "$yateaExec" "$yateaDir"
    fi
  else
    cd "$installDir"
    for f in Unitex*; do
      unitexDir="$f"
    done
  fi

  echo "TESTING ALL (except YaTeA) ... "
  echo "**** TreeTagger"
  "$thisScriptDir"/install_tree_tagger.sh -t "TreeTagger"
  echo "**** TagEN 2"
  "$thisScriptDir"/install_tagen2.sh -t "tagen2" "$unitexDir"
  pwd
  echo "**** LIA TAGG"
  "$thisScriptDir"/install_lia_tagg.sh -t "lia_tagg"
  echo "**** LIA NE"
  "$thisScriptDir"/install_lia_ne.sh -t "lia_ne" "lia_tagg"

  cd ..
}



function fullPathDir {
  dirBak=$(pwd)
  cd "$1"
  pwd
  cd "$dirBak"
}


main "$@"
cd "$currDir"


