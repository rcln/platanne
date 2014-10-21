#!/bin/csh

# Compile ressources for bracketting

if ( $#argv < 2 ) then
 echo 'Syntax: lia_make_bracketlex.csh <bracket_pattern file> <lm.arpa.sirlex file>'
else
 cut -f2 $1 | $LIA_TAGG/bin/lia_sort_lexicon | \
	$LIA_TAGG/bin/lia_rajoute_code > $1.sirlex
 $LIA_TAGG/bin/lia_compile_lexique $1.sirlex
 $LIA_TAGG/bin/lia_compile_tree_phrase $2 $1.sirlex $1 $1
 rm $1.sirlex
endif
  
