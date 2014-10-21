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

all :
	subdir='src' ; \
	echo "Making all in $$subdir" ; \
	cd $$subdir && $(MAKE) all
	subdir='biglex_ne' ; \
	echo "Making all in $$subdir" ; \
	cd $$subdir && $(MAKE) all -f makefile.biglex

# Cleaning binaries
clean :
	subdir='src' ; \
	echo "Making clean in $$subdir" ; \
	cd $$subdir && $(MAKE) clean
	subdir='biglex_ne' ; \
	echo "Making clean in $$subdir" ; \
	cd $$subdir && $(MAKE) clean -f makefile.biglex

  
