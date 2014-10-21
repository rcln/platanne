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

# Makefile ModuleGram #

all : $(LIA_TAGG)/bin/lia_compile_ml \
      $(LIA_TAGG)/bin/lia_compile_lexique \
      $(LIA_TAGG)/bin/lia_compile_arpa \
      $(LIA_TAGG)/bin/lia_compile_pmc \
      $(LIA_TAGG)/bin/lia_rajoute_lemme_ecg \
      $(LIA_TAGG)/bin/lia_rajoute_code \
      $(LIA_TAGG)/bin/lia_test_lexique \
      $(LIA_TAGG)/bin/lia_word2lemmes \
      $(LIA_TAGG)/object/proba_mot_classe.o \
	  $(LIA_TAGG)/object/filter_lexicon

$(LIA_TAGG)/bin/lia_compile_ml: compile_modele_langage.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/compile_gram.o \
		$(LIA_TAGG)/object/proba_mot_classe.o \
		$(LIA_TAGG)/object/gere_lexique.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_compile_ml compile_modele_langage.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/compile_gram.o \
		$(LIA_TAGG)/object/proba_mot_classe.o \
		$(LIA_TAGG)/object/gere_lexique.o $(LIBS)

$(LIA_TAGG)/bin/lia_compile_arpa: compile_arpa.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/compile_gram.o \
		$(LIA_TAGG)/object/proba_mot_classe.o \
		$(LIA_TAGG)/object/gere_lexique.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_compile_arpa compile_arpa.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/proba_mot_classe.o \
		$(LIA_TAGG)/object/compile_gram.o \
		$(LIA_TAGG)/object/gere_lexique.o $(LIBS)

$(LIA_TAGG)/bin/lia_compile_lexique: compile_lexique.c $(LIA_TAGG)/object/gere_lexique.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_compile_lexique compile_lexique.c \
		$(LIA_TAGG)/object/gere_lexique.o

$(LIA_TAGG)/bin/lia_compile_pmc: compile_pmc.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o
	 $(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_compile_pmc compile_pmc.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o $(LIBS)

$(LIA_TAGG)/bin/lia_rajoute_lemme_ecg: rajoute_lemme_ecg.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o
	 $(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_rajoute_lemme_ecg rajoute_lemme_ecg.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o $(LIBS)

$(LIA_TAGG)/bin/lia_word2lemmes: word2lemmes.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o
	 $(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_word2lemmes word2lemmes.c \
		$(LIA_TAGG)/object/libgram.o \
		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o $(LIBS)

$(LIA_TAGG)/bin/lia_rajoute_code: rajoute_code.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_rajoute_code rajoute_code.c

$(LIA_TAGG)/bin/lia_test_lexique: test_lexique.c \
		$(LIA_TAGG)/object/gere_lexique.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_test_lexique test_lexique.c \
		$(LIA_TAGG)/object/gere_lexique.o

$(LIA_TAGG)/object/libgram.o: libgram.c
	$(CC) $(OPTIONS) -c libgram.c
	mv libgram.o $(LIA_TAGG)/object/libgram.o

$(LIA_TAGG)/object/compile_gram.o: compile_gram.c
	$(CC) $(OPTIONS) -c compile_gram.c
	mv compile_gram.o $(LIA_TAGG)/object/compile_gram.o

$(LIA_TAGG)/object/gere_lexique.o: gere_lexique.c
	$(CC) $(OPTIONS) -c gere_lexique.c
	mv gere_lexique.o $(LIA_TAGG)/object/gere_lexique.o

$(LIA_TAGG)/object/proba_mot_classe.o: proba_mot_classe.c
	$(CC) $(OPTIONS) -c proba_mot_classe.c
	mv proba_mot_classe.o $(LIA_TAGG)/object/proba_mot_classe.o
 
$(LIA_TAGG)/object/filter_lexicon: filter_lexicon.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/filter_lexicon filter_lexicon.c
 
