#!/bin/csh
#
# Make the lexicon ressources for the tagger & bracketter
#
# parameters :
#              $1 = lexicon.txt (word,POS,freq,lemma)
#	       $2 = 3class LM arpa format
#              $3 = optionnal [-accent] : ressource for French reaccentueur

set LANG = C

set LC_ALL = C

if ( $#argv < 2 ) then
 echo 'Syntax: lia_make_datalex.csh <lexicon.txt> <LM3class.arpa> [-accent]'
else
 echo "Make lexicon"
 $LIA_TAGG/bin/lia_eclate_lexique_union < $1 >! $1.compte
 if ( $3 == "-accent") then
  $LIA_TAGG/bin/lia_eclate_lexique_union < $1 | $LIA_TAGG/bin/lia_produit_lex_reacc >! $1.accent.compte
 endif
 
 echo "    - graph lexicon"
 cut -f1 $1.compte | $LIA_TAGG/bin/lia_sort_lexicon >! $1.graf
 $LIA_TAGG/bin/lia_rajoute_code < $1.graf >! $1.sirlex
 $LIA_TAGG/bin/lia_compile_lexique $1.sirlex
 if ( $3 == "-accent") then
  echo "    - accent"
  cut -f1 $1.accent.compte | $LIA_TAGG/bin/lia_sort_lexicon > $1.accent.graf
  $LIA_TAGG/bin/lia_rajoute_code < $1.accent.graf > $1.accent.sirlex
  $LIA_TAGG/bin/lia_compile_lexique $1.accent.sirlex
 endif

 echo "    - class lexicon"
 $LIA_TAGG/bin/lia_extract_lex_from_arpa < $2 >! $2.sirlex
 $LIA_TAGG/bin/lia_compile_ml $2.sirlex $2 log_10 3g -dicho

 echo "Make PMC model"
 echo "    - lemma"
 $LIA_TAGG/bin/lia_compile_pmc $1.sirlex $2.sirlex $1.compte lemme log_10 $1 
 if ( $3 == "-accent") then
  echo "    - accent"
  $LIA_TAGG/bin/lia_compile_pmc $1.accent.sirlex $2.sirlex $1.accent.compte lemme log_10 $1.accent
 endif
 
 echo Nettoyage
 rm $1.compte $1.sirlex $2.sirlex
 if ( $3 == "-accent") then
  rm $1.accent.compte $1.accent.sirlex
 endif 
 echo Termine

endif 
 
