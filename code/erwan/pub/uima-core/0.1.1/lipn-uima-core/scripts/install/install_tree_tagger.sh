#!/bin/bash

# EM 14/09/10
# may not work !
#

currDir=$(pwd)
url="ftp://ftp.ims.uni-stuttgart.de/pub/corpora/tree-tagger-linux-3.2.tar.gz"
urlScripts="ftp://ftp.ims.uni-stuttgart.de/pub/corpora/tagger-scripts.tar.gz"
urlInstall="ftp://ftp.ims.uni-stuttgart.de/pub/corpora/install-tagger.sh"
urlsData="ftp://ftp.ims.uni-stuttgart.de/pub/corpora/bulgarian-par-linux-3.1.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/dutch-par-linux-3.1.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/dutch2-par-linux-3.1.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/english-par-linux-3.1.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/french-par-linux-3.2.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/french-par-linux-3.2-utf8.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/german-par-linux-3.2.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/greek-par-linux-3.2.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/italian-par-linux-3.1.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/italian-par-linux-3.2-utf8.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/italian-par2-linux-3.1.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/spanish-par-linux-3.1.bin.gz ftp://ftp.ims.uni-stuttgart.de/pub/corpora/swahili-par-linux-3.2.bin.gz http://corpus.leeds.ac.uk/mocky/russian.par.gz"
testOnly=0
installDir="TreeTagger"

function printHelp {
    echo  >/dev/stderr
    echo "This script will try to install TreeTagger, and will likely fail for future versions." >/dev/stderr
    echo  >/dev/stderr
    echo "Syntax: $0 [options]" >/dev/stderr
    echo "  Options:" >/dev/stderr
    echo "    -h help " >/dev/stderr
    echo "    -d <target dir> Install in this dir" >/dev/stderr
    echo "    -t <TreeTagger dir> test only" >/dev/stderr
    echo  >/dev/stderr
    exit $1
}


function main {

  while getopts 'd:ht:' option ; do # parsing options 
    case $option in
      "d" ) cd "$OPTARG";;
      "h" ) printHelp 0;;
      "t" ) installDir="$OPTARG"
            testOnly=1;;
      "?" ) printHelp 1;;
    esac
  done
  shift $(($OPTIND - 1)) 

  if [ $testOnly -eq 0 ]; then
    mkdir "$installDir"
    cd "$installDir"
    echo -e "\nDownloading TreeTagger...\n"
    wget "$url"
    archive=$(basename "$url")
    if [ ! -f $archive ]; then
      echo -e "Can not find archive file: $archive" >/dev/stderr
      exit 2
    fi

    echo -e "\nDownloading TreeTagger scripts...\n"
    wget "$urlScripts"
    wget "$urlInstall"
    installScript=$(basename "$urlInstall")
    if [ ! -f $archive ]; then
      echo -e "Can not find script file: $installScript" >/dev/stderr
      exit 2
    fi

    echo -e "\nDownloading TreeTagger parameter files...\n"
    n=1
    for f in $urlsData; do
      echo "Parameter file $n : $(basename $f)"
      wget "$f"
      n=$(( $n + 1 ))
    done
    
    echo -e "\nRunning TreeTagger install script ...\n"
    chmod a+x "$installScript"
    ./"$installScript"
    rm -f *.gz
    cd ..

  fi

  echo -e "Testing... "
  cd "$installDir"
  echo "This example sentence is very simple." | cmd/utf8-tokenize.perl | cmd/tree-tagger-english

  echo -e "\nHope it worked: should be ok if you see something which looks like a parse.\n"
  cd "$currDir"
}




main "$@"
