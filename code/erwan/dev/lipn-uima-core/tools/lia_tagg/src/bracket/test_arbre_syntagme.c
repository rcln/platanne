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
/*  Programme de test du stockage des decomposition des groupes de syntagme
    dans un arbre. En entree, il faut le nom generique des fichiers
    codant l'arbre : <nom>.syntree_des <nom>.syntree_tab <nom>.syntree_zon
    Il faut aussi un dico des groupes syntaxiques compile au format ML
    et un dico des categories synt formant les decompositions au format ML  */
/*  FRED 0499  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#include <libgram.h>
#include <arbre_syntagme.h>

#define TailleLigne	200

/*................................................................*/

/* quelques variables resolument globales */

ty_lexique Lexique_Classe,Lexique_Groupe; /* lexique des classe et des groupes de syntagmes */

ty_desc_syntagme pt_syntagme;

/*................................................................*/

void test_arbre(char *ch)
{
wrd_index_t c_code;
short g_code,*zone /*,last_gcode*/ ;
int i,si_fin,i_tab,nb_gs;
char *ch_gs;

ch[strlen(ch)-1]='\0';
/*printf("%s",ch);*/

/*last_gcode=-1;*/

for(i_tab=0,ch=strtok(ch,"_ \t\n");(i_tab!=-1)&&(ch);ch=strtok(NULL,"_ \t\n"))
 {
 if (!Mot2Code(ch,&c_code,Lexique_Classe))
  { fprintf(stderr,"HO : c'est quoi ce code : %s\n",ch); exit(0); }
  
 i_tab=acces_syntagme(c_code,i_tab,&zone,&nb_gs,pt_syntagme);
 
 if (zone)
  {
  printf("%s ->",ch);

  for(i=0;i<nb_gs;i++)
   {
   g_code=zone[i];
   if (g_code<0) { si_fin=1; g_code=-g_code; } else si_fin=0;
/* if (si_fin) last_gcode=g_code; */
   
   if (!Code2Mot((wrd_index_t)g_code,&ch_gs,Lexique_Groupe))
    { fprintf(stderr,"HO : c'est quoi ce gs_code : %d\n",g_code); exit(0); }
   printf(" %s",ch_gs);
   if (si_fin) printf("*");
   
   }
  printf("\n");
  }
 }
/*
if (last_gcode!=-1)
 {
 if (!Code2Mot((wrd_index_t)last_gcode,&ch_gs,Lexique_Groupe))
  { fprintf(stderr,"HO : c'est quoi ce gs_code : %d\n",g_code); exit(0); }
 printf("\t%s\n",ch_gs);
 }
else printf("\tXXXX\n");
*/
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne];

if ((argc==1)||((argc<4)&&(strcmp(argv[1],"-h"))))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <dico classe syntaxique> <dico groupe syntagme> \
 <nom arbre>\n",argv[0]);
 exit(0);
 }

if (!strcmp(argv[1],"-h"))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <dico classe syntaxique> <dico groupe syntagme> \
 <nom arbre>\n\n\
 \t Programme de test du stockage des decomposition des groupes de syntagme\n\
 \t Il faut, en entree :\n\
 \t  * <dico classe syntaxique> : dico compile au format ML des classes syntaxique composant les\n\
 \t                               syntagmes.\n\
 \t  * <dico groupe syntagme>   : dico compile au format ML des groupes de syntagmes.\n\
 \t Le nom du modele sert a charger les 3 fichiers suivants :\n\
 \t  * <nom arbre>.syntree_des : description de la taille des tableaux\n\
 \t  * <nom arbre>.syntree_tab : arbre des decompositions codees dans un tableau\n\
 \t  * <nom arbre>.syntree_zon : zone des stockage des codes des groupes syntaxiques associes a\n\
 \t                              chaque noeud\n\n",argv[0]);
 exit(0);
 }

fprintf(stderr,"Chargement des lexiques -> ");
Lexique_Classe=ChargeLexique(argv[1]);
Lexique_Groupe=ChargeLexique(argv[2]);
fprintf(stderr,"Termine\n");

fprintf(stderr,"Chargement de l'arbre des syntagmes -> ");
pt_syntagme=charge_desc_syntagme(argv[3]);
fprintf(stderr,"Termine\n");

while(fgets(ch,TailleLigne,stdin)) test_arbre(ch);
}
 
