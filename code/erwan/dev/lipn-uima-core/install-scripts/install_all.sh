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
    echo  
    echo "This script will try to install all programs used by the lipn uima platform" 
    echo " in a directory named '$installDir'" 
    echo "YaTeA must have been installed before running this script (or use option -n" 
    echo "if you want all tools but YaTeA)."
    echo  
    echo "Syntax: $0 [options] <path to 'yatea' executable> <path to 'YaTeA' data directory>" 
    echo "  Options:" 
    echo "    -h help " 
    echo "    -d <target dir> Install in this dir (dir '$installDir' will be created there)" 
    echo "    -t <tools dir> test only" 
    echo "    -n don't want yatea stuff (no mandatory args needed in this case)"
    echo  
    echo "CAUTION: this script is really unsure, probably that most tools won't be "
    echo "         installed. But it is not unsafe, nothing is deleted."
    echo
    echo " Warning: most of the tools can not be installed in a path containing whitespaces"
    echo  
    echo "-> All sub-scripts named 'install_xxx' must be in the same dir than this script"
    echo  
    echo "INFOS : - yatea exec is generally located in a 'bin' dir, e.g. /usr/local/bin" 
    echo "        - YaTeA dir is generally located in a 'share' dir, e.g. /usr/local/share" 
    echo  
    echo " YaTeA is a Perl module, you can use 'cpan' utility to install it:"
    echo "install Lingua::YaTeA   (but don't be surprised if that fails...)"
    echo " For more details about YaTeA install, please refer to the manual." 
    echo  
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
      "?" ) printHelp 1 >/dev/stderr;;
    esac
  done
  shift $(($OPTIND - 1)) 
  if [ $# -ne 2 ] && [ $noYatea -eq 0 ]; then
    echo "ERROR: you must provide path to YaTeA executable and to YaTeA directory." > /dev/stderr
    printHelp 1 >/dev/stderr
  fi
  if [ $noYatea -ne 1 ]; then 
    yateaExec="$1"
    yateaDir="$2"
    verifDir "$yateaDir"
  fi

  if [ $testOnly -eq 0 ]; then
    mkdir "$installDir"
    cd "$installDir"
    echo "####  INSTALLING ALL ..."

    if [ $noYatea -eq 0 ]; then
      "$thisScriptDir"/make_yatea_links.sh $(fullPathDir "$yateaExec") $(fullPathDir "$yateaDir")
    fi

    "$thisScriptDir"/install_tree_tagger.sh

    "$thisScriptDir"/install_unitex.sh
    for f in Unitex*; do
      unitexDir="$f"
    done

    "$thisScriptDir"/install_tagen2.sh $(fullPathDir "$unitexDir")
    for f in tagen*; do
      tagenDir="$f"
    done
    ln -s "$tagenDir" "tagen2"

    "$thisScriptDir"/install_lia_tagg.sh

    "$thisScriptDir"/install_crf++.sh
    for f in CRF*; do
      crfppDir="$f"
    done
    ln -s "$crfppDir" "crf++"

    "$thisScriptDir"/install_lia_ne.sh $(fullPathDir lia_tagg) $(fullPathDir "crf++")
    for f in lia_ne*; do
      liaNeDir="$f"
    done
    ln -s "$liaNeDir" "lia_ne"

  else
    cd "$installDir"
    for f in Unitex*; do
      unitexDir="$f"
    done
  fi

  echo
  echo "### TESTING ALL (except YaTeA) ... ### "
  echo
  echo "**** TreeTagger"
  "$thisScriptDir"/install_tree_tagger.sh -t $(fullPathDir "TreeTagger")
  echo "**** TagEN 2"
  "$thisScriptDir"/install_tagen2.sh -t $(fullPathDir "tagen2") $(fullPathDir "$unitexDir")
  pwd
  echo "**** LIA TAGG"
  "$thisScriptDir"/install_lia_tagg.sh -t $(fullPathDir "lia_tagg")
  echo "**** LIA NE"
  "$thisScriptDir"/install_lia_ne.sh -t $(fullPathDir "lia_ne") $(fullPathDir "lia_tagg") $(fullPathDir "crf++")

  cd ..
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


