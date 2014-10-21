export CLASSPATH=$PLATANNE_HOME/code/erwan/dev/lipn-uima-core/target/lipn-uima-core-0.1.3-SNAPSHOT.jar:$PLATANNE_HOME/code/erwan/dev/lipn-nlptools-utils/target/lipn-nlptools-utils-0.2.2-SNAPSHOT.jar

./lipn-run-cpe.sh tests/CPEs/tt-multitag-fr-small-iso.xml

annotationViewer.sh
