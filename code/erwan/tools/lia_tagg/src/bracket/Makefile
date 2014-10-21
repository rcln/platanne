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

all :   $(LIA_TAGG)/bin/lia_compile_tree_phrase \
	$(LIA_TAGG)/bin/lia_test_tree_phrase \
	$(LIA_TAGG)/bin/lia_bracket_phrase

clean :
 

$(LIA_TAGG)/bin/lia_compile_tree_phrase: compile_arbre_syntagme.c
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_compile_tree_phrase compile_arbre_syntagme.c \
	$(LIA_TAGG)/object/gere_lexique.o $(LIBS)

$(LIA_TAGG)/bin/lia_test_tree_phrase: test_arbre_syntagme.c $(LIA_TAGG)/object/arbre_syntagme.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_test_tree_phrase test_arbre_syntagme.c \
	$(LIA_TAGG)/object/arbre_syntagme.o $(LIA_TAGG)/object/gere_lexique.o

$(LIA_TAGG)/bin/lia_bracket_phrase: parse_syntagme.c $(LIA_TAGG)/object/arbre_syntagme.o
	$(CC) $(OPTIONS) -o $(LIA_TAGG)/bin/lia_bracket_phrase parse_syntagme.c \
	$(LIA_TAGG)/object/arbre_syntagme.o $(LIA_TAGG)/object/gere_lexique.o

$(LIA_TAGG)/object/arbre_syntagme.o: arbre_syntagme.c
	$(CC) $(OPTIONS) -c arbre_syntagme.c
	mv arbre_syntagme.o $(LIA_TAGG)/object/arbre_syntagme.o
 
