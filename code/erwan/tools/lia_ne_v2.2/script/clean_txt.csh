#!/bin/csh

if ( "$1" == "-nocap" || "$2" == "-nocap" ) then
 set BIGLEX = biglex_ne.minu
else
 set BIGLEX = biglex_ne
endif


setenv LIA_TAGG_LANG french
sed 's/-/_/g' | sed 's/\\&amp;/et/g' | \
	$LIA_TAGG/bin/trans_apos -deglue -space | \
        $LIA_TAGG/bin/lia_tokenize $LIA_NE/biglex_ne/$BIGLEX.apos.tab $1 | \
        $LIA_TAGG/bin/trans_apos -glue | \
        $LIA_TAGG/bin/lia_sentence $LIA_TAGG/data/list_chif_virgule.fr.tab | \
        $LIA_TAGG/bin/lia_nett_capital $LIA_TAGG/data/lex80k.fr.tab -no_a | \
	$LIA_TAGG/bin/lia_nomb2alpha $LIA_TAGG/data/list_chif_virgule.fr.tab | \
        $LIA_TAGG/bin/lia_unmotparligne | \
	grep -v '<s>' | grep -v '</s>' | $LIA_NE/bin/onelinex | sed 's/ _ / /g'
  
