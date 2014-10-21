#!/bin/bash

# EM jul 2011
# patch for 2 bugs in yatea which (sometimes) causes the 2 following errors:
# 1) 
# Can't locate object method "getFather" via package "Lingua::YaTeA::RootNode" at /home/erwan/local/perl_modules/lib/perl5/Lingua/YaTeA/Node.pm line 1496.
# 2) 
# Can't call method "getIndex" on an undefined value at /home/erwan/local/perl_modules/lib/perl5/Lingua/YaTeA/Node.pm line 2101

patternA="if(\$index > \$to_insert)"
patternB="if(\$index < \$to_insert)"
patternBug2="\$new_next = \$node->searchHead(0);"
offset1=22
offset2=11
offsetBug2=0
data1=")  # PATCH EM\n"
data2="(  # PATCH EM\n"
dataBug2="if (defined(\$new_next) && (\$new_next->getIndex > \$index)) # PATCH EM\n#"

patternCorpus="\$mean_occ = \$total_occ / scalar keys %\$term_candidates_h;"
dataCorpus="\$mean_occ = (scalar(keys %\$term_candidates_h) > 0)?(\$total_occ / scalar keys %\$term_candidates_h):'NaN'; # PATCH EM\n#"
offsetCorpus=-1

function insertLine {
  target="$1"
  pattern="$2"
  data="$3"
  offset="$4"
  echo "Patching '$target' ... "
  cp "$target" "$target.tmp"
  nb=$(grep -n "$pattern" "$target.tmp" | cut -d":" -f1)
  nb=$(( $nb  + $offset ))
#  echo "debug: line $nb data $data"
  head -n $nb "$target.tmp" > "$target"
  echo -e -n "$data" >> "$target"
  nb=$(( $nb  + 1 ))
  tail -n +$nb "$target.tmp" >> "$target"
  rm -f "$target.tmp"
}


if [ $# -ne 1 ]; then
  echo "Syntax: $0 <path to perl module Lingua/YaTeA"  2>&1
  echo "Warning: you must have write permission to this directory" 2>&1
  exit 1
fi
path="$1"
if [ ! -f "$path/Node.pm" ] || [ ! -f "$path/Corpus.pm" ] ; then
  echo "Error: file(s) Node.pm and/or Corpus.pm not found in $path" 2>&1
  exit 2
fi
file1="$path/Node.pm"
file2="$path/Corpus.pm"

echo "Patching main script '$file'..." 
cp "$file1" "$file1.bak"
cp "$file2" "$file2.bak"
insertLine "$file1" "$patternA" "$data1" "$offset1"
insertLine "$file1" "$patternA" "$data2" "$offset2"
insertLine "$file1" "$patternB" "$data1" "$offset1"
insertLine "$file1" "$patternB" "$data2" "$offset2"
insertLine "$file1" "$patternBug2" "$dataBug2" "$offsetBug2"
insertLine "$file2" "$patternCorpus" "$dataCorpus" "$offsetCorpus"

exit 0


#### END OF PATCHING SCRIPT #####
