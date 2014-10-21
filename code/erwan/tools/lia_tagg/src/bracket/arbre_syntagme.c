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
/*  Fonctions d'utilisation de l'arbre des groupes de syntagmes  */
/*  FRED 0499  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#include <libgram.h>
#include <arbre_syntagme.h>

#define TailleLigne	200

/*................................................................*/

/* chargement de l'arbre dans les tableaux */

ty_desc_syntagme charge_desc_syntagme(char *nomfich)
{
char ch[TailleLigne];
FILE *file;
ty_desc_syntagme pt;

pt=(ty_desc_syntagme)malloc(sizeof(struct type_desc_syntagme));

sprintf(ch,"%s.syntree_des",nomfich);
if (!(file=fopen(ch,"rt")))
 { fprintf(stderr,"Can't open %s\n",ch); exit(0); }
fgets(ch,TailleLigne,file); fgets(ch,TailleLigne,file);
sscanf(ch,"%d %d",&(pt->t_tabl),&(pt->t_zone));
fclose(file);

pt->tabl=(type_syntab *)malloc(sizeof(type_syntab)*pt->t_tabl);
pt->zone=(short *)malloc(sizeof(short)*pt->t_zone);

sprintf(ch,"%s.syntree_tab",nomfich);
if (!(file=fopen(ch,"rb")))
 { fprintf(stderr,"Can't open %s\n",ch); exit(0); }
fread(pt->tabl,sizeof(type_syntab),pt->t_tabl,file);
fclose(file);

sprintf(ch,"%s.syntree_zon",nomfich);
if (!(file=fopen(ch,"rb")))
 { fprintf(stderr,"Can't open %s\n",ch); exit(0); }
fread(pt->zone,sizeof(short),pt->t_zone,file);
fclose(file);

return pt;
}

/*................................................................*/

/*  Accede aux donnees de l'arbre  */

int acces_syntagme(
	wrd_index_t c_code,		/* code cate syntaxique */
	int i_tab,			/* indice en cours ds l'arbre de syntagme (0 au debut) */
	short **zone,			/* pointeur vers une zone contenant les codes des groupes */
	int *nb_gs,			/* nb de code de groupes dans la zone */
	ty_desc_syntagme pt_syntagme	/* arbre representant les syntagmes */
	)
{
while ((i_tab<pt_syntagme->t_tabl)&&(i_tab!=-1)&&((int)c_code!=pt_syntagme->tabl[i_tab].c_code))
 i_tab=pt_syntagme->tabl[i_tab].fd;

if (i_tab>=pt_syntagme->t_tabl)
 {
 fprintf(stderr,"ERROR : depassement sur i_tabl=%d et t_tabl=%d\n",i_tab,pt_syntagme->t_tabl);
 exit(0);
 }

if ((i_tab!=-1)&&((int)c_code==pt_syntagme->tabl[i_tab].c_code))
 {
 *zone=&(pt_syntagme->zone[pt_syntagme->tabl[i_tab].i_gs]);
 *nb_gs=pt_syntagme->tabl[i_tab].nb_gs;
 return pt_syntagme->tabl[i_tab].fg;
 }

*zone=NULL; *nb_gs=-1;
return -1;
}
  
