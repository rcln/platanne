/*
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
*/
/*  .................................................  */
/*  Lits le fichier tri lettre d'une serie de classes  */
/*  .................................................  */

typedef struct type_class
	{
	char *label;
	int taille,nbtri,nbmots,code;
	ty_tri tree;
	double score;
	} *ty_class;

extern int NbClass;

/*  Allocation memoire  */

ty_class NewTabClass();

void LibereClass();

void AfficheClass();

void CompleteClasse();

void AffecteScore();

void SortScore();

void AfficheScore();

void TrouvePlace();

void RangeScore();

/*  Retablissement des accents  */

void accent_mot();
  
