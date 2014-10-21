#!/bin/csh -f

# args : $1=file generic  $2=minu/capi

set FILE = $1

if ( $2 == "minu" ) then
 cat $FILE.word.txt | $LIA_NE/script/utf82iso8859 | sed 's/-/_/g' |  sed "s/'_/'/g" |\
       $LIA_NE/bin/decapital | \
       $LIA_TAGG/bin/lia_quicktagg \
                -lextag $LIA_NE/biglex_ne/ester_train_biglex_ne.arpa.sirlex \
                -morpho NULL \
                -lexgraf $LIA_NE/biglex_ne/biglex_ne.minu.sirlex \
                -pmc $LIA_NE/biglex_ne/biglex_ne.minu \
                -ml $LIA_NE/biglex_ne/ester_train_biglex_ne.arpa | \
	$LIA_NE/bin/fmt4crf | \
        crf_test -m $LIA_NE/crf_data/model_ne.minu | \
        $LIA_NE/bin/tagg2text |\
        $LIA_NE/bin/rewrite_token_ne > $FILE.word.ne
else
 cat $FILE.word.txt | $LIA_NE/script/utf82iso8859 | sed 's/-/_/g' |  sed "s/'_/'/g" |\
       $LIA_TAGG/bin/lia_quicktagg \
                -lextag $LIA_NE/biglex_ne/ester_train_biglex_ne.arpa.sirlex \
                -morpho NULL \
                -lexgraf $LIA_NE/biglex_ne/biglex_ne.sirlex \
                -pmc $LIA_NE/biglex_ne/biglex_ne \
                -ml $LIA_NE/biglex_ne/ester_train_biglex_ne.arpa | \
	$LIA_NE/bin/fmt4crf | \
        crf_test -m $LIA_NE/crf_data/model_ne | \
        $LIA_NE/bin/tagg2text |\
        $LIA_NE/bin/rewrite_token_ne > $FILE.word.ne
endif
cat $FILE.id.txt | grep -v '<s>' | grep -v '</s>' > $FILE.id.txt2
cat $FILE.word.ne | grep -v '<s>' | grep -v '</s>' > $FILE.word.ne2
paste $FILE.id.txt2 $FILE.word.ne2 | grep -v '<s>' | grep -v '</s>' > $FILE.id_word.ne

