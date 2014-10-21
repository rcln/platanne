#!/bin/csh

setenv LIA_TAGG_LANG french

sed 's/-/_/g' | sed 's/\\&amp;/et/g' | $LIA_NE/lia_biglex_ne/script/lia_clean.biglex_ne 
 
