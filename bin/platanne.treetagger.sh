#!/bin/bash

#
# JGF Oct 2013
# this script is an equivalent to UIMA script "lipn-run-cpe.sh", but configured to be run 
# a from the command line of the lipn-rcln server (blackbox type)
# Run with -h for details.
#
# It depends on two environment variables: 
#     $PLATANNE_HOME: root directory of the PLATANNE annotation platform http://lipn.univ-paris13.fr/rcln/wiki/index.php/Platanne
#     $UIMA_HOME: root directory of the local UIMA install
#
#TODO 
# 1) refaire à la manière d'Erwan, avec help et main et tout ça
# main "$@"
# exit $?
# 2) process pathfile with several file instad of one by one (it raises the different encoding problem)
# 3) treat files with spaces in the name; example: "Mi grabación #1.wav"
# 4) Exit if the encoding is unknown or if the file doesn't exists, etc... 
# 5) Verify if language is in the [fr,es,en,ar,it] set
# 6) Oliver Twist in english

LIPN_CORE=$PLATANNE_HOME/code/erwan/dev/lipn-uima-core/
PLATANNE_RUN=$LIPN_CORE/platanne.run

export CLASSPATH=$PLATANNE_HOME/code/erwan/dev/lipn-uima-core/target/lipn-uima-core-0.1.3-SNAPSHOT.jar:$PLATANNE_HOME/code/erwan/dev/lipn-nlptools-utils/target/lipn-nlptools-utils-0.2.2-SNAPSHOT.jar


function printHelp {
    echo  >/dev/stderr
    echo "Syntax: $0 [options] <input_file>" >/dev/stderr
    echo "  Options:" >/dev/stderr
    echo "    -h help " >/dev/stderr
    echo "    -l <language> from the following language list: [en,fr,es,it] " >/dev/stderr
    echo "    -d debug mode: it doesn't erase the output files from " > /dev/stderr
    echo "    $LIPN_CORE/platanne.run" >/dev/stderr
    echo  >/dev/stderr
    exit $1    
}

debugMode=0

function main {

    while getopts 'dl:h' option ; do
	case $option in
	    h) printHelp 0;;
	    d) debugMode=1;;
	    l) language="$OPTARG";;
	    ?) printHelp 0;;
	esac
    done
    shift $(($OPTIND - 1)) 
    if [ $1  ]; then
	input_file=$1
	test -n "$language" || language="fr"
	test -n "$debugMode" && echo "Starting processing $1 with options language=$language; debug_mode=$debugMode" > /dev/stderr
    else
        echo "Missing input file" > /dev/stderr
	printHelp
    fi


    #1 read input arguments <file_name> [language]  
    # 	default values are language=fr

    base_input_file=$(basename $input_file)

    #2 create a new tmp dir in $PLATANNE_HOME/code/erwan/dev/platanne.run/input
    #  create a new tmp dir in $PLATANNE_HOME/code/erwan/dev/platanne.run/output

    #command to generate a random 8 characters string
    tmp_name=$(cat /dev/urandom | tr -cd 'a-f0-9' | head -c 8)

    mkdir $PLATANNE_RUN/input/tmp.$tmp_name
    mkdir $PLATANNE_RUN/input/tmp.$tmp_name/cpe
    mkdir $PLATANNE_RUN/output/tmp.$tmp_name

    #3 copy input file name to $PLATANNE_HOME/code/erwan/dev/lipn-uima-core/input
    cp $1 $PLATANNE_RUN/input/tmp.$tmp_name/


    #4 copy the CPE model to the input directory
    tmp_cpe_name=tt-multitag-fr-platanne-$tmp_name.xml
    input_cpe=$PLATANNE_RUN/input/tmp.$tmp_name/cpe/$tmp_cpe_name
    output_cpe=$PLATANNE_RUN/output/tmp.$tmp_name/$tmp_cpe_name

    cp $PLATANNE_RUN/CPEs/tt-multitag-fr-platanne-model.xml $input_cpe

    #5 set the tmp_name_directory into $PLATANNE_HOME/code/erwan/dev/platanne.run/CPEs/tt-multitag-fr-platanne-iso.xml	
		# <collectionReader> 		
		# <name>InputDirectory</name>
		# <string>platanne.run/input/tmp_name_directory/</string>
		# and
		# <casProcessors>
		# <name>OutputDirectory</name>
		# <string>platanne.run/output/tmp_name_directory/</string>

    cmd="sed -i 's/tmpname_to_replace/tmp.$tmp_name/g' $input_cpe"
    eval $cmd

    #6 detect input file encoding
    encoding=$(file  $PLATANNE_RUN/input/tmp.$tmp_name/$base_input_file | egrep -o '(UTF-8|ISO-8859|ASCII)')

    #7 set encoding into into $PLATANNE_HOME/code/erwan/dev/platanne.run/CPEs/tt-multitag-fr-platanne-iso.xml
		# <collectionReader> 		
		# <name>Encoding</name>
		# <string>ISO8859-1</string>		

    #TODO: exit 1 if encoding other than ASCII, ISO or UTF-8
    test "$encoding" == "ISO-8859" && encoding="ISO8859-1"
    if [ $encoding == "UTF-8" ]; then
	    encoding="utf8"
	    #removing eventual BOM charachters from UTF-8
	    cmd="sed -i 's/\xef\xbb\xbf//g' $PLATANNE_RUN/input/tmp.$tmp_name/$base_input_file"
	    eval $cmd
    fi
    
    test -n "$debugMode" && echo "File encoding:$encoding" > /dev/stderr


    cmd="sed -i 's/encoding_to_replace/$encoding/g' $input_cpe"
    test -n "$encoding" && eval $cmd|| echo "Encoding other than UTF-8 or ISO-8859" 

    #8 set language into $PLATANNE_HOME/code/erwan/dev/platanne.run/CPEs/tt-multitag-fr-platanne-iso.xml
		# <collectionReader> 		
		# <name>Language</name>
		# <string>fr</string>


    cmd="sed -i 's/language_to_replace/$language/g' $input_cpe"
    eval $cmd

    #9 run lipn-uima-core.treetagger
    pushd $LIPN_CORE >&2
    ./lipn-run-cpe.sh $input_cpe >&2
    popd >&2
    cat $PLATANNE_RUN/output/tmp.$tmp_name/$base_input_file.xmi
    if [ $debugMode -eq 1 ]; then
	echo "Input written on  $PLATANNE_RUN/input/tmp.$tmp_name/" > /dev/stderr
	echo "Ouptut written on  $PLATANNE_RUN/output/tmp.$tmp_name/" > /dev/stderr
    else
	rm -fr $PLATANNE_RUN/input/tmp.$tmp_name
	rm -fr $PLATANNE_RUN/output/tmp.$tmp_name
    fi
}

main "$@"
