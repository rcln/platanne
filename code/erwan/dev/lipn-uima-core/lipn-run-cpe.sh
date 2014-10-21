#!/bin/bash

#
# EM Sept 2010
# this script is an equivalent to UIMA script "runCPE.sh", but configured to be run 
# the "lipn environment" context.
# Run with -h for details.
#
#

logConfigFile="conf/Logger.properties"
libsPath="lib"
dontUseUIMAPath=0
myClasspath="$(pwd)/bin:$CLASSPATH"
javaRunCpeClass="org.apache.uima.examples.cpe.SimpleRunCPE"
verbose=0


function printHelp {
    echo  
    echo "Run a UIMA CPE either using UIMA standard way (UIMA_HOME must be set), " 
    echo "or using CLASSPATH. In the latter case don't forget to provide UIMA libs." 
    echo "Libraries to add to CLASSPATH can be put in '$libsPath' directory or specified" 
    echo "using -l and/or -d options" 
    echo "The most simple way to use this script is to run it from project root, but" 
    echo "options can be used to run it from anywhere (notice that you will probably have"  
    echo "more path issues to deal with in that case)" 
    echo  
    echo "Syntax: $0 [options] <CPE descriptor>" 
    echo "  Options:" 
    echo "    -h help " 
    echo "    -m <mem> Java memory (e.g. '512m')" 
    echo "    -c <paths> paths/JARs to add to CLASSPATH (separated by ':')" 
    echo "    -d <dirs> directories from which all JARs files will be added to" 
    echo "       CLASSPATH (default='$libsPath', space separated)" 
    echo "    -l <log config> specify logger config file" 
    echo "    -n don't use UIMA_HOME path (all libs must be provided)"  
    echo "    -v verbose mode (prints the command before running it)"  
    echo  
    exit $1
}


function main {

  while getopts 'nhm:c:d:l:v' option ; do # parsing options 
    case $option in
      "n" ) dontUseUIMAPath="1";;
      "m" ) javaMem="-Xms$OPTARG -Xmx$OPTARG";;
      "c" ) myClasspath="$OPTARG:$myClasspath";;
      "d" ) libsPath="$OPTARG $libsPath";;
      "l" ) logConfigFile="$OPTARG";;
      "h" ) printHelp 0;;
      "v" ) verbose=1;;
      "?" ) printHelp 1 >/dev/stderr;;
    esac
  done
  shift $(($OPTIND - 1)) 
  if [ $# -ne 1 ]; then
    echo "Missing argument" >/dev/stderr
    printHelp 1 >/dev/stderr
  fi
  cpe="$1"

  if [ -z "$JAVA_HOME" ]; then
    echo "Warning: no JAVA_HOME specified." >/dev/stderr
    runJava="java"
  else
    runJava="$JAVA_HOME/bin/java"
  fi

  if [ ! -e "$logConfigFile" ]; then
    echo "Warning: log config file $logConfigFile not found, ignoring it." >/dev/stderr
    logConfigFile=""
  fi

  for d in $libsPath; do
    for f in $d/*; do
      myClasspath="$f:$myClasspath"
    done
  done
  if [ -z "$UIMA_HOME" ] || [ "$dontUseUIMAPath" -eq 1 ]; then
    if [ $verbose -eq 1 ]; then
      echo "Using Java basic call."
    fi
    args="$javaMem -cp $myClasspath"
    if [ ! -z "$logConfigFile" ]; then
       args="$args -Djava.util.logging.config.file=\"$logConfigFile\""
    fi
    cmd="$runJava $args $javaRunCpeClass $cpe"
  else 
    if [ $verbose -eq 1 ]; then
	echo "Using UIMA standard call."
    fi
    if [ ! -z "$javaMem" ];
      then javaMem="export UIMA_JVM_OPTS=\"$javaMem\"; "
    fi
    if [ ! -z "$logConfigFile" ]; then
      logConfigFile="export UIMA_LOGGER_CONFIG_FILE=\"$logConfigFile\"; "
    fi
    cmd="export UIMA_CLASSPATH=$myClasspath; $javaMem $logConfigFile $UIMA_HOME/bin/runCPE.sh  $cpe"
  fi

  if [ $verbose -eq 1 ]; then
    echo "Executing command:"
    echo "$cmd"
    echo "CLASSPATH is:"
    echo "$CLASSPATH"
  fi


  eval $cmd
  return $?

}



main "$@"
exit $?
