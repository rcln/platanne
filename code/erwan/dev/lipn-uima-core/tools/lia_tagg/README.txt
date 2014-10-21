
     --------------------------------------------------------
     LIA_TAGG: a statistical POS tagger + syntactic bracketer
     --------------------------------------------------------
 
     Copyright (C) 2001 FREDERIC BECHET
 
     ..................................................................
 
     This file is part of LIA_TAGG
 
     LIA_TAGG is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
     ..................................................................
 
     Contact :
               FREDERIC BECHET - LIA - UNIVERSITE D'AVIGNON
               AGROPARC BP1228 84911  AVIGNON  CEDEX 09  FRANCE
               frederic.bechet@lia.univ-avignon.fr
     ..................................................................
 
 --------
 VERS 1.1
 --------

 ------
 README
 ------

  I   : Content of the package LIA_TAGG
  II  : Installation
  III : Useful scripts
  IV  : An example
  V   : Data format
  VI  : Warning
 

I - Content of the package LIA_TAGG
-----------------------------------

Once you have uncompress and untar the file 'lia_tagg.tar.gz',
a directory 'lia_tagg' is created, containing the following
files and directories:

  - Makefile : compiling sources and data files of LIA_TAGG
  - README   : this present file
  - /bin     : directory which will contain the executable files
  - /data    : directory containing the ressources for French and English tagging
  - /doc     : documentation about LIA_TAGG as follows
  - /object  : object files generated
  - /script  : script command for using LIA_TAGG
  - /src     : source files contained in the following directories
	- format		   : tokenization
	- libgram		   : n-gram LM
	- tagg			   : POS tagging + text accentuation
	- bracket		   : syntactic bracketing




II - Installation
-----------------

LIA_TAGG is composed of a set of modules all written in
standard C, using only standard libraries. It has been
successfully compiled on the following UNIX environments:
- HP-UX
- SUN-SOLARIS
- IRIX
- LINUX

1) To install the package, you must first set the environment
   variable: 'LIA_TAGG' with the path of the root directory 'lia_tagg'.

   For example, in c-shell:

   setenv LIA_TAGG /usr/tools/lia_tagg
   
   Then you have to set the language (French or English). To do so, set the
   environment variable 'LIA_TAGG_LANG' to either 'french' or 'english'.
   
2) Then edit the file 'Makefile' in order to change, if
   necessary the C-compiler and the compilation options
   which will be used to compile the package.

3) Compile the package with the command: 'make all'

4) Compile the resources. If you want only the French (resp. English) ressources,
   just type 'make ressource.french' (resp. 'make ressource.english').
   If you want both, type: 'make ressource.all'

That's all !!

If you want to suppress the compiled files, just execute:

make clean
make clean_ressource




III - Useful scripts
--------------------

LIA_TAGG contains a set of scripts in order to clean, format, tagg and bracket
French or English texts. Here are a quick overview of each command:


lia_clean               : "clean" a raw text, tokenize the words, split the text into
                          sentences, remove the capital letter at the beginning of each
                          sentence if necessary and output one word on each line.
                          By default, the text is considered as a stream of character, all
                          the paragraph mark ('\n') are suppressed. If you want to keep them,
                          use the option '-keep_fmt'

lia_tagg  [-guess]      : tagg each word with its POS, one word on each line, 1st field=word,
                          2nd field=POS. For the French tagger, a guesser is provided
                          (a program that guesses a POS for all non proper-name unknown words).
                          To activate this option add '-guess' to the command 'lia_tagg'.
                          WARNING: this option is valid ONLY for the French language.

lia_tagg+lemm [-guess]  : same as 'lia_tagg' but add a field with the lemm (for French only at the moment)

lia_tagg+reacc [-guess] : same as 'lia_tagg' but tries to put back the accent for non-accentutate
                          texts. WARNING: this option is valid ONLY for the French language.
lia_bracket [1/2/3]     : bracketter that cut a sentence according to a shallow parsing process
                          that matches POS patterns into syntactic phrases. Three different output
                          are proposed (option 1 (default), 2 or 3)

lia_tagg2name           : take as input a text processed by 'lia_tagg' or 'lia_tagg+reacc' and
                          output the potential proper-name expressions based on simple rules using
                          POS information as well as graphical ones.
                          Example: lia_clean | lia_tagg | lia_tagg2name

IV - An example
---------------

Each step in the text processing will be now illustrated on this
small raw text example in French:
--
Ceci est un test. De plus en plus, le 1 janvier tombe un lundi. Il couttent
vrraiment 1,08 euros. Il eleve un debat deja eleve sur le TALN.
--


1) Formatting the text
This is done with the script: $LIA_TAGG/script/lia_nett
- input = stdin
- output= stdout

This script tokenize the text according to the lexicon used by the
POS tagger. Then, the text is split into sentences, marked by the
tags <s> (beginning of a sentence) and </s> (end of a sentence).
The eventual capital letter of each first word of a sentence is then
removed.
Finally, digit strings are converted into words with respect to the symbol ','
(French ONLY) and the output text contains one word on each line.

This is the result of this script on the previous text:

<s>
eci
est
un
test
.
</s>
<s>
de_plus_en_plus
,
le
premier
janvier
tombe
un
lundi
.
</s>
<s>
il
couttent
vrraiment
un
virgule
zéro
huit
euros
.
</s>
<s>
il
eleve
un
debat
deja
eleve
sur
le
TALN
.
</s>


2) POS tagging + accentuation
This is done by the script: $LIA_TAGG/script/lia_tagg+reacc -guess
   - input  : stdin
   - output : stdout
This script takes the output of 'lia_nett' and process a POS tagging and
a text accentuation. The out-of-vocabulary words are processed by a POS guesser
(for example, the words 'couttent' and 'vrraiment' in the previous example),
except for unknown proper-names (capitalize words within a sentence) which
remain with the tag 'MOTINC'.

This is the result of this script on the output of 'lia_nett':

<s> ZTRM
eci ADV
est VE3S
un DETMS
test NMS
. YPFOR
</s> ZTRM
<s> ZTRM
de_plus_en_plus ADV
, YPFAI
le DETMS
premier AMS
janvier NMS
tombe V3S
un DETMS
lundi NMS
. YPFOR
</s> ZTRM
<s> ZTRM
il PPER3MS
couttent V3S
vrraiment ADV
un DETMS
virgule CHIF
zéro CHIF
huit CHIF
euros NMP
. YPFOR
</s> ZTRM
<s> ZTRM
il PPER3MS
élève V3S
un DETMS
débat NMS
déjà ADV
élevé AMS
sur PREP
le DETMS
TALN MOTINC
. YPFOR
</s> ZTRM


3) Bracketting
This is done by the script: $LIA_TAGG/script/lia_bracket
   - input  : stdin
   - output : stdout
This script takes the output of 'lia_tagg' and perform a bracketting process according
to syntactic phrase patterns. This is the result of this script on the output of 'lia_tagg+reacc':

[<s>]                                       ZTRM        [ZTRM]
[eci]                                     MOTINC        [MOTINC]
[est]                                         VS        [VE3S]
[un test]                                    NMS        [DETMS NMS]
[.]                                        YPFOR        [YPFOR]
[</s>]                                      ZTRM        [ZTRM]
[<s>]                                       ZTRM        [ZTRM]
[,]                                        YPFAI        [YPFAI]
[le premier janvier]                         NMS        [DETMS AMS NMS]
[tombe]                                       VS        [V3S]
[un lundi]                                   NMS        [DETMS NMS]
[.]                                        YPFOR        [YPFOR]
[</s>]                                      ZTRM        [ZTRM]
[<s>]                                       ZTRM        [ZTRM]
[il]                                     PPER3MS        [PPER3MS]
[couttent]                                MOTINC        [MOTINC]
[vrraiment]                               MOTINC        [MOTINC]
[un]                                       DETMS        [DETMS]
[virgule zéro huit]                         CHIF        [CHIF CHIF CHIF]
[euros]                                   MOTINC        [MOTINC]
[.]                                        YPFOR        [YPFOR]
[</s>]                                      ZTRM        [ZTRM]
[<s>]                                       ZTRM        [ZTRM]
[il]                                     PPER3MS        [PPER3MS]
[élève]                                       VS        [V3S]
[un débat déjà élevé]                        NMS        [DETMS NMS ADV AMS]
[sur]                                       PREP        [PREP]
[le]                                       DETMS        [DETMS]
[TALN]                                    MOTINC        [MOTINC]
[.]                                        YPFOR        [YPFOR]
[</s>]                                      ZTRM        [ZTRM]



V - Data format
---------------

LIA_TAGG uses several kind of resources:

1- Lexicons
2 Part-Of-Speech resources

You can update any of these resources and the following
quick description of each of them should answer most of the
questions. BUT, don't forget to compile again the resources
each time you modify one of the data files.


1- Lexicons

There is 1 lexicons used by LIA_TAGG (except the POS resources):
- list_chif_virgule
  List of words possibly following a digit string
  Format of each line: word


2- Part-Of-Speech resources

The main editable resource of the POS tagging is the dictionary. The one
given with LIA_TAGG contains 10K words, and is stored in the text file: lex10K
The format of the dictionary is as follows:

- one word on each line
- format of each line:
word POS1 freq(word,POS1) lemma(word,{POS1) POS2 freq(word,POS2) lemma(word,POS2) ....

For example:
assise AFS 213 assis NFS 733 assise VPPFS 320 asseoir

To add a new word, just add a new line at this file, and if you
don't have a corpus for estimating the frequency of each
couple (word,POS), just put '1' instead.

VI - Warning

!! !! Be careful of the accent encoding of the files !! !!
Check that the accents of the lexicon 'data/lex80k.fr' are
correct (ASCII  ISO8859-1). If not, you have to use an
accent transcoder to correct them into ISO8859-1. This transcoding MUST
be applied to ALL the files in the directories /data
and /src

When using LIA_TAGG with another encoding, you must encode your
text before and after LIA_TAGG. For example :

iconv -t iso-8859-1 -f utf-8 | lia_clean | lia_tagg+lemm | iconv -t utf-8 -f iso-8859-1

Contact
-------

If you have any problem or question, contact me at :

frederic.bechet@lif.univ-mrs.fr
 
