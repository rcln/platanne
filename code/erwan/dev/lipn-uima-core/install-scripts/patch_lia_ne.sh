#!/bin/bash

# EM sept 2010
# patch for a path bug in LIA NE script and .h files in CRF++ 0.41 dir 
# => won't work in further versions (if any) !!!
#

target1="script/ne_tagg.csh"
nb1=16
target2="CRF++-0.41/common.h"
patt2="#include <cstring>"
toInsert2="#include <cstdio>"
target3="CRF++-0.41/freelist.h"
patt3="#include <vector>"
toInsert3="#include <cstring>"


function insertLine {
  target="$1"
  pattern="$2"
  data="$3"
  echo "Patching '$target' ... "
  cp "$target" "$target.bak"
  nb=$(grep -n "$pattern" "$target.bak" | cut -d":" -f1)
  head -n $nb "$target.bak" > "$target"
  nb=$(( $nb + 1 ))
  echo "$data" >> "$target"
  tail -n +$nb "$target.bak" >> "$target"
}

if [ $# -ne 1 ]; then
  echo >/dev/stderr
  echo "Syntax: $0 <path to LIA_NE>" >/dev/stderr
  echo "This patch will correct a bug about LIA TAGG path in the LIA NE main script" >/dev/stderr
  echo >/dev/stderr
  exit 1
fi
dir="$1"

insertLine "$dir/$target2" "$patt2" "$toInsert2"

insertLine "$dir/$target3" "$patt3" "$toInsert3"

echo "Patching main script '$target1'..." 
tail -n $nb1 "$0" > "$dir"/$target1
exit 0


#### END OF PATCHING SCRIPT #####

##!/bin/csh

# PATCHED VERSION

$LIA_NE/bin/cut_hansard | \
	$LIA_TAGG/bin/lia_tokenize $LIA_TAGG/data/lex80k.fr.tab -lazy | \
	$LIA_TAGG/bin/concat_apos $LIA_NE/data/lex_apos.txt | \
	$LIA_TAGG/bin/lia_sentence $LIA_TAGG/data/list_chif_virgule.fr.tab | \
	$LIA_TAGG/bin/remove_1stcapital $LIA_NE/data/list_nomcommun.biglex | \
	$LIA_TAGG/bin/unmotparligne | \
	$LIA_TAGG/script/lia_tagg | \
	$LIA_NE/bin/fmt4crf | \
	$LIA_NE/CRF++-0.41/crf_test -m $LIA_NE/data/model4.bin | \
	$LIA_NE/bin/tagg2text | \
	$LIA_NE/bin/postprocess_ne -lex $LIA_NE/data/lex_ALL_NP.code

