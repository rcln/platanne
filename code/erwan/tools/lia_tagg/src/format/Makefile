#
#    --------------------------------------------------------
#    LIA_TAGG: a statistical POS tagger + syntactic bracketer
#    --------------------------------------------------------
#
#    Copyright (C) 2001 FREDERIC BECHET
#
#    ..................................................................
#
#    This file is part of LIA_TAGG
#
#    LIA_TAGG is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
#    ..................................................................
#
#    Contact :
#              FREDERIC BECHET - LIA - UNIVERSITE D'AVIGNON
#              AGROPARC BP1228 84911  AVIGNON  CEDEX 09  FRANCE
#              frederic.bechet@lia.univ-avignon.fr
#    ..................................................................
#
# Makefile : formatage et nettoyage de textes
# FRED 0299

all : $(LIA_TAGG)/bin/lia_compile_lexitree \
      $(LIA_TAGG)/bin/lia_tokenize \
      $(LIA_TAGG)/bin/lia_sentence \
      $(LIA_TAGG)/bin/lia_nett_capital \
      $(LIA_TAGG)/bin/lia_rm_ponct \
      $(LIA_TAGG)/bin/lia_nomb2alpha \
      $(LIA_TAGG)/bin/lia_unmotparligne \
      $(LIA_TAGG)/bin/lia_split_sentence \
      $(LIA_TAGG)/bin/lia_ecg2sent \
      $(LIA_TAGG)/bin/lia_sort_lexicon \
      $(LIA_TAGG)/bin/reacc_1champs_bdlex2win \
      $(LIA_TAGG)/bin/reacc_bdlex2win \
      $(LIA_TAGG)/bin/reacc_mac2ux \
      $(LIA_TAGG)/bin/reacc_ux2bdlex \
      $(LIA_TAGG)/bin/reacc_win2bdlex \
      $(LIA_TAGG)/bin/reacc_1champs_win2bdlex \
      $(LIA_TAGG)/bin/report_OOV \
      $(LIA_TAGG)/bin/stat_corpus \
      $(LIA_TAGG)/bin/remove_1stcapital \
      $(LIA_TAGG)/bin/last_capital_clean \
      $(LIA_TAGG)/bin/detokenize \
      $(LIA_TAGG)/bin/intersec \
      $(LIA_TAGG)/bin/unephraseparligne \
      $(LIA_TAGG)/bin/concat_apos \
      $(LIA_TAGG)/bin/cut_sent \
      $(LIA_TAGG)/bin/rewrite_corpus \
      $(LIA_TAGG)/bin/from_code2word \
      $(LIA_TAGG)/bin/from_lex2code \
      $(LIA_TAGG)/bin/trans_apos \
      $(LIA_TAGG)/bin/clean_biglex \
      $(LIA_TAGG)/bin/rm_tags \
      $(LIA_TAGG)/bin/decapital \
      $(LIA_TAGG)/bin/reacc_lemm_biglex_bdlex2win \
      $(LIA_TAGG)/bin/sort_arpa \
      $(LIA_TAGG)/bin/sublex \
      $(LIA_TAGG)/bin/add_code_to_lex_phon \
      $(LIA_TAGG)/bin/format_lex \
      $(LIA_TAGG)/bin/redecoupe_mostfreq


$(LIA_TAGG)/bin/lia_compile_lexitree: CompileLexiTree.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_compile_lexitree CompileLexiTree.c

$(LIA_TAGG)/bin/lia_tokenize: DecoupeMots.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_tokenize DecoupeMots.c

$(LIA_TAGG)/bin/lia_sentence: DecoupePhrases.c $(LIA_TAGG)/object/GestionTablMots.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_sentence DecoupePhrases.c \
		$(LIA_TAGG)/object/GestionTablMots.o

$(LIA_TAGG)/bin/lia_nett_capital: EliminePremMajuscule.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_nett_capital EliminePremMajuscule.c

$(LIA_TAGG)/bin/lia_rm_ponct: EnlevePonctuation.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_rm_ponct EnlevePonctuation.c

$(LIA_TAGG)/bin/lia_nomb2alpha: Nombre2Alpha.c \
		$(LIA_TAGG)/object/GestionTablMots.o \
		$(LIA_TAGG)/object/chif_to_alpha.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_nomb2alpha Nombre2Alpha.c \
		$(LIA_TAGG)/object/GestionTablMots.o \
		$(LIA_TAGG)/object/chif_to_alpha.o

$(LIA_TAGG)/bin/lia_unmotparligne: UnMotParLigne.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_unmotparligne UnMotParLigne.c

$(LIA_TAGG)/bin/lia_split_sentence: split_sentence.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_split_sentence split_sentence.c

$(LIA_TAGG)/object/GestionTablMots.o: GestionTablMots.c
	$(CC) $(OPTIONS) -c GestionTablMots.c
	mv GestionTablMots.o $(LIA_TAGG)/object/GestionTablMots.o

$(LIA_TAGG)/object/chif_to_alpha.o: chif_to_alpha.c
	$(CC) $(OPTIONS) -c chif_to_alpha.c 
	mv chif_to_alpha.o $(LIA_TAGG)/object/chif_to_alpha.o

$(LIA_TAGG)/bin/lia_ecg2sent: ecg2sent.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_ecg2sent ecg2sent.c

$(LIA_TAGG)/bin/reacc_1champs_bdlex2win: reacc_1champs_bdlex2win.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/reacc_1champs_bdlex2win reacc_1champs_bdlex2win.c

$(LIA_TAGG)/bin/reacc_bdlex2win: reacc_bdlex2win.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/reacc_bdlex2win reacc_bdlex2win.c

$(LIA_TAGG)/bin/reacc_mac2ux: reacc_mac2ux.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/reacc_mac2ux reacc_mac2ux.c

$(LIA_TAGG)/bin/reacc_ux2bdlex: reacc_ux2bdlex.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/reacc_ux2bdlex reacc_ux2bdlex.c

$(LIA_TAGG)/bin/reacc_win2bdlex: reacc_win2bdlex.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/reacc_win2bdlex reacc_win2bdlex.c

$(LIA_TAGG)/bin/reacc_1champs_win2bdlex: reacc_1champs_win2bdlex.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/reacc_1champs_win2bdlex reacc_1champs_win2bdlex.c

$(LIA_TAGG)/bin/report_OOV: report_OOV.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/report_OOV report_OOV.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/object/lia_tree_avl.o: lia_tree_avl.c
	$(CC) $(OPTIONS) -c lia_tree_avl.c
	mv lia_tree_avl.o $(LIA_TAGG)/object

$(LIA_TAGG)/object/lia_liblex.o: lia_liblex.c
	$(CC) $(OPTIONS) -c lia_liblex.c
	mv lia_liblex.o $(LIA_TAGG)/object

$(LIA_TAGG)/bin/stat_corpus: stat_corpus.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/stat_corpus stat_corpus.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/remove_1stcapital: remove_1stcapital.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/remove_1stcapital remove_1stcapital.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/last_capital_clean: last_capital_clean.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/last_capital_clean last_capital_clean.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/detokenize: detokenize.c
	 $(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/detokenize detokenize.c

$(LIA_TAGG)/bin/intersec: intersec.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/intersec intersec.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/unephraseparligne: unephraseparligne.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/unephraseparligne unephraseparligne.c

$(LIA_TAGG)/bin/concat_apos: concat_apos.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/concat_apos concat_apos.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/cut_sent: cut_sent.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/cut_sent cut_sent.c

$(LIA_TAGG)/bin/rewrite_corpus: rewrite_corpus.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/rewrite_corpus rewrite_corpus.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/from_code2word: from_code2word.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/from_code2word from_code2word.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/from_lex2code: from_lex2code.c $(LIA_TAGG)/object/lia_tree_avl.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/from_lex2code from_lex2code.c $(LIA_TAGG)/object/lia_tree_avl.o

$(LIA_TAGG)/bin/trans_apos: trans_apos.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/trans_apos trans_apos.c

$(LIA_TAGG)/bin/clean_biglex: clean_biglex.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/clean_biglex clean_biglex.c

$(LIA_TAGG)/bin/rm_tags: rm_tags.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/rm_tags rm_tags.c

$(LIA_TAGG)/bin/decapital: decapital.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/decapital decapital.c

$(LIA_TAGG)/bin/reacc_lemm_biglex_bdlex2win: reacc_lemm_biglex_bdlex2win.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/reacc_lemm_biglex_bdlex2win reacc_lemm_biglex_bdlex2win.c
 
$(LIA_TAGG)/bin/sort_arpa: sort_arpa.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/sort_arpa sort_arpa.c

$(LIA_TAGG)/bin/lia_sort_lexicon: sort_lexicon.c $(LIA_TAGG)/object/lia_liblex.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_sort_lexicon sort_lexicon.c $(LIA_TAGG)/object/lia_liblex.o

$(LIA_TAGG)/bin/sublex: sublex.c $(LIA_TAGG)/object/lia_liblex.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/sublex sublex.c $(LIA_TAGG)/object/lia_liblex.o

$(LIA_TAGG)/bin/redecoupe_mostfreq: redecoupe_mostfreq.c $(LIA_TAGG)/object/lia_liblex.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/redecoupe_mostfreq redecoupe_mostfreq.c $(LIA_TAGG)/object/lia_liblex.o

$(LIA_TAGG)/bin/add_code_to_lex_phon: add_code_to_lex_phon.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/add_code_to_lex_phon add_code_to_lex_phon.c

$(LIA_TAGG)/bin/format_lex: format_lex.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/format_lex format_lex.c

