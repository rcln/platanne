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
#
#  Makefile LIA_TAGG  #

# C Compiler definition
#
CC = gcc -m32

# Math library
#
LIBS = -lm

# Include files
#
INCLUDES = -I. -I$(LIA_TAGG)/src/libgram -I$(LIA_TAGG)/src/format

# C compiler options (default=NONE)
#
OPTIONS = $(INCLUDES)

# Source compilation

MAKE = make LIBS="$(LIBS)" INCLUDES="$(INCLUDES)" OPTIONS="$(OPTIONS)" CC="$(CC)"

all :
	@set fnord $(MAKEFLAGS); \
        subdir='$(LIA_TAGG)/src/format' ; \
	echo "Making all in $$subdir" ; \
	cd $$subdir && $(MAKE) all
	@set fnord $(MAKEFLAGS); \
	subdir='$(LIA_TAGG)/src/libgram' ; \
	echo "Making all in $$subdir" ; \
	cd $$subdir && $(MAKE) all
	@set fnord $(MAKEFLAGS); \
	subdir='$(LIA_TAGG)/src/tagg' ; \
	echo "Making all in $$subdir" ; \
	cd $$subdir && $(MAKE) all
	@set fnord $(MAKEFLAGS); \
	subdir='$(LIA_TAGG)/src/bracket' ; \
	echo "Making all in $$subdir" ; \
	cd $$subdir && $(MAKE) all

# Cleaning binaries
clean :
	rm $(LIA_TAGG)/bin/*
	rm $(LIA_TAGG)/object/*

# Compilation of data files

ressource.all: ressource.english ressource.french

ressource.english:
	set LANG = C
	chmod ug+x $(LIA_TAGG)/script/*
	csh $(LIA_TAGG)/script/lia_make_datalex.csh $(LIA_TAGG)/data/lex150k.en \
		$(LIA_TAGG)/data/lm3class.en.arpa
	$(LIA_TAGG)/bin/lia_compile_lexitree $(LIA_TAGG)/data/lex150k.en \
		$(LIA_TAGG)/data/lex150k.en.tab
	$(LIA_TAGG)/bin/lia_compile_lexitree $(LIA_TAGG)/data/list_chif_virgule.en \
                $(LIA_TAGG)/data/list_chif_virgule.en.tab
	csh $(LIA_TAGG)/script/lia_make_bracketlex.csh $(LIA_TAGG)/data/bracket_pattern.en \
		$(LIA_TAGG)/data/lm3class.en.arpa.sirlex

ressource.french:
	set LANG = C
	chmod ug+x $(LIA_TAGG)/script/*
	csh $(LIA_TAGG)/script/lia_make_datalex.csh $(LIA_TAGG)/data/lex80k.fr \
		$(LIA_TAGG)/data/lm3class.fr.arpa -accent
	$(LIA_TAGG)/bin/lia_compile_lexitree $(LIA_TAGG)/data/lex80k.fr \
		$(LIA_TAGG)/data/lex80k.fr.tab
	$(LIA_TAGG)/bin/lia_compile_lexitree $(LIA_TAGG)/data/list_chif_virgule.fr \
		$(LIA_TAGG)/data/list_chif_virgule.fr.tab
	$(LIA_TAGG)/bin/lia_compile_lexitree $(LIA_TAGG)/data/list_chif_virgule.fr.bdlex \
		$(LIA_TAGG)/data/list_chif_virgule.fr.bdlex.tab
	csh $(LIA_TAGG)/script/lia_make_bracketlex.csh $(LIA_TAGG)/data/bracket_pattern.fr \
		$(LIA_TAGG)/data/lm3class.fr.arpa.sirlex
 
# Cleaning binary data files
clean_ressource:
	rm $(LIA_TAGG)/data/lex*k.??.*
	rm $(LIA_TAGG)/data/lm3class.??.arpa.*
	rm $(LIA_TAGG)/data/bracket_pattern.??.*
	rm $(LIA_TAGG)/data/list_chif_virgule.??.tab
 
  
