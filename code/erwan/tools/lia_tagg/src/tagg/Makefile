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
# Makefile QuickTagg #

all : $(LIA_TAGG)/bin/lia_quicktagg \
      $(LIA_TAGG)/bin/lia_eclate_lexique_union \
      $(LIA_TAGG)/bin/lia_extract_lex_from_arpa \
      $(LIA_TAGG)/bin/lia_produit_lex_reacc \
      $(LIA_TAGG)/bin/lia_extract_name

clean :
 
$(LIA_TAGG)/bin/lia_quicktagg: QuickTagg.c \
		$(LIA_TAGG)/object/libgram.o \
 		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o \
		$(LIA_TAGG)/object/quicktagg_lib.o \
		$(LIA_TAGG)/object/GereTreeMorpho.o \
		$(LIA_TAGG)/object/LitTriMorpho.o \
		$(LIA_TAGG)/object/module_morpho.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_quicktagg QuickTagg.c \
		$(LIA_TAGG)/object/libgram.o \
 		$(LIA_TAGG)/object/gere_lexique.o \
		$(LIA_TAGG)/object/proba_mot_classe.o \
		$(LIA_TAGG)/object/quicktagg_lib.o \
		$(LIA_TAGG)/object/GereTreeMorpho.o \
		$(LIA_TAGG)/object/LitTriMorpho.o \
		$(LIA_TAGG)/object/module_morpho.o \
		$(LIBS)

$(LIA_TAGG)/object/quicktagg_lib.o: quicktagg_lib.c
	$(CC) $(OPTIONS) -c quicktagg_lib.c
	mv quicktagg_lib.o $(LIA_TAGG)/object/quicktagg_lib.o

# Module DevinMorpho #

OPTIONS_MORPHO = -DMAGIC_N=7

$(LIA_TAGG)/object/GereTreeMorpho.o: GereTreeMorpho.c
	$(CC) -c $(OPTIONS) $(OPTIONS_MORPHO) GereTreeMorpho.c
	mv GereTreeMorpho.o $(LIA_TAGG)/object/GereTreeMorpho.o

$(LIA_TAGG)/object/LitTriMorpho.o: LitTriMorpho.c
	$(CC) -c $(OPTIONS) $(OPTIONS_MORPHO) LitTriMorpho.c
	mv LitTriMorpho.o $(LIA_TAGG)/object/LitTriMorpho.o

$(LIA_TAGG)/object/module_morpho.o: module_morpho.c
	$(CC) -c $(OPTIONS) $(OPTIONS_MORPHO) module_morpho.c
	mv module_morpho.o $(LIA_TAGG)/object/module_morpho.o

# tools
 
$(LIA_TAGG)/bin/lia_eclate_lexique_union: eclate_lexique_union.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_eclate_lexique_union eclate_lexique_union.c

$(LIA_TAGG)/bin/lia_extract_lex_from_arpa: extract_lex_from_arpa.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_extract_lex_from_arpa extract_lex_from_arpa.c

$(LIA_TAGG)/bin/lia_produit_lex_reacc: produit_lex_reacc.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_produit_lex_reacc produit_lex_reacc.c

$(LIA_TAGG)/bin/lia_extract_name: extract_name.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_extract_name extract_name.c

