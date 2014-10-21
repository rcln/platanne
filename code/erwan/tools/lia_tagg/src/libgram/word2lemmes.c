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
/*  affiche la liste des lemmes pour un mot donne  */
/*  FRED 0799 - Modif 1100  - Modif 1105  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <libgram.h>

#define TailleLigne	4000

ty_lexique LexiqueGraf,LexiqueCate;
ty_pmc ModelePmc;

#define MAX_LEMM	20

int main(argc,argv)
 int argc;
 char **argv;
{
char ch[TailleLigne],*pt,*lemm;
wrd_index_t tabl[MAX_LEMM],c_graf;
logprob_t tpro[MAX_LEMM];
int nb,i;

if (argc<3) { fprintf(stderr,"Syntaxe : %s <dico mot> <modele pmc/lemme>\n",argv[0]); exit(0); }

LexiqueGraf=ChargeLexique(argv[1]);
ModelePmc=charge_pmc(argv[2]);

while(fgets(ch,TailleLigne,stdin))
 for(pt=strtok(ch," \n\t");pt;pt=strtok(NULL," \n\t"))
  {
  printf("%s\n",pt);
  if ((Mot2Code(pt,&c_graf,LexiqueGraf))&&(pmc_liste_mot_lemmes(tabl,tpro,&nb,c_graf,ModelePmc,0)==CORRECT)&&(nb>0))
   {
   for(i=0;i<nb;i++)
    if (Code2Mot(tabl[i],&lemm,LexiqueGraf)) printf("%s\n",lemm); else fprintf(stderr,"Strange: unknown lemm:%s\n",lemm);
   }
  }

exit(0);
}
 
