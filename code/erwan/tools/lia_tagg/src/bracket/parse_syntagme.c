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
/*  Programme de parsing d'un fichier .ecg envoye sur l'entree standard.
    En entree, il faut le nom generique des fichiers codant l'arbre :
    <nom>.syntree_des <nom>.syntree_tab <nom>.syntree_zon
    Il faut aussi un dico des groupes syntaxiques compile au format ML
    et un dico des categories synt formant les decompositions au format ML
    En sortie : le fichier .ecg parse sur la sortie standard  */
/*  FRED 0499  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#include <libgram.h>
#include <arbre_syntagme.h>

#define TailleLigne	200

#define debug	0

/*................................................................*/

/* quelques variables resolument globales */

ty_lexique Lexique_Classe,Lexique_Groupe; /* lexique des classe et des groupes de syntagmes */

ty_desc_syntagme pt_syntagme;

/*  Stockage de la phrase avant traitement  */

#define MaxMotsParPhrase	1000
#define TailleMaxMot		60
#define GRAF			0
#define CATE			1

char Phrase[MaxMotsParPhrase][2][TailleMaxMot];

/*................................................................*/

void printf_format1(int deb_s,int fin_s,char *cate) /* avec des tabulations */
{
int i,t;
printf("[");
for(i=deb_s,t=0;i<fin_s;i++)
 {
 printf("%s ",Phrase[i][GRAF]);
 t+=strlen(Phrase[i][GRAF])+1; 
 }
printf("%s]  ",Phrase[i][GRAF]); t+=strlen(Phrase[i][GRAF])+4;
for(i=t;i<40;i++) printf(" ");
printf("%8s\t[",cate);
for(i=deb_s;i<fin_s;i++) printf("%s ",Phrase[i][CATE]);
printf("%s]\n",Phrase[i][CATE]);
}

void printf_format2(int deb_s,int fin_s,char *cate) /* par bloc */
{
int i;
printf("|");
for(i=deb_s;i<fin_s;i++) printf("%s ",Phrase[i][GRAF]);
printf("%s|",Phrase[i][GRAF]);
printf("%s|",cate);
for(i=deb_s;i<fin_s;i++) printf("%s ",Phrase[i][CATE]);
printf("%s|\n",Phrase[i][CATE]);
}

void printf_format3(int deb_s,int fin_s,char *cate) /* par unite */
{
int i;
printf("[");
for(i=deb_s;i<fin_s;i++) printf("(%s#%s)",Phrase[i][GRAF],Phrase[i][CATE]);
printf("(%s#%s)|%s]",Phrase[i][GRAF],Phrase[i][CATE],cate);
}

void printf_format(int deb_s,int fin_s,char *cate,char choix_affiche)
{
switch (choix_affiche)
 {
 case '1' : printf_format1(deb_s,fin_s,cate); break;
 case '2' : printf_format2(deb_s,fin_s,cate); break;
 case '3' : printf_format3(deb_s,fin_s,cate); break;
 }
}

int parse_chaine(int posi,int vide_tout,char choix_affiche)
{
static int i_tab=0,nb_gs=0,nb_decal=0,deb_s=0,fin_s=0;
static short *zone,last_gs=-1;
static wrd_index_t c_code;
static int i;
static char *ch_gs;

if (debug)
 printf("TEST vide_tout=%d last_gs=%d i_tab=%d posi=%d %s %s\n",vide_tout,last_gs,i_tab,posi,Phrase[posi][GRAF],Phrase[posi][CATE]); 

if (!vide_tout)
 {
 if (!Mot2Code(Phrase[posi][CATE],&c_code,Lexique_Classe))
  { fprintf(stderr,"HO : c'est quoi ce code : %s\n",Phrase[posi][CATE]); exit(0); }
 i_tab=acces_syntagme(c_code,i_tab,&zone,&nb_gs,pt_syntagme);

 if (debug) printf("\tTEST2 : zone=%s nb_gs=%d\n",zone?"XX":"NULL",nb_gs);
 }
else zone=NULL;

if (zone)
 {
 fin_s=posi;
 for(i=0;(i<nb_gs)&&(zone[i]>0);i++); /* WARNING !!!! ADV MUST BE A SYNTAGME (SYNT 0 MUST BE TERMINAL !! BUG BUG */
 if (i<nb_gs) { last_gs=-zone[i]; nb_decal=0; } else nb_decal++;
 if (debug) printf(" \t\tTEST3 : i=%d nb_gs=%d last_gs=%d nb_decal=%d\n",i,nb_gs,last_gs,nb_decal);

 return posi+1;
 }
else
 if (last_gs!=-1) /* pour les categories ne faisant pas partie des groupes de syntagmes (VEPPRE) */
  {
  if (!Code2Mot((wrd_index_t)last_gs,&ch_gs,Lexique_Groupe))
   { fprintf(stderr,"HO : c'est quoi ce gs_code : %d\n",last_gs); exit(0); }
  if (debug) printf(" \t\t\tTEST4 : deb_s=%d fin_s=%d nb_decal=%d posi=%d ch_gs=%s\n",deb_s,fin_s,nb_decal,posi,ch_gs);
  printf_format(deb_s,fin_s-nb_decal,ch_gs,choix_affiche);
  if (choix_affiche=='3') printf(" ");
  i=nb_decal; /* nb de case en arriere pour obtenir la derniere categorie qui etait terminale */
  i_tab=0; nb_gs=0; last_gs=-1; nb_decal=0;
  if (vide_tout) deb_s=0; else deb_s=posi-i;
  return deb_s;
  }
 else
  {
  if (debug) printf(" \t\t\tTEST5 : posi=%d cate=%s\n",posi,Phrase[posi][CATE]);
  i_tab=0; nb_gs=0; last_gs=-1; nb_decal=0;
  printf_format(posi,posi,Phrase[posi][CATE],choix_affiche);
  if (choix_affiche=='3') printf(" ");
  if (vide_tout) deb_s=0; else deb_s=posi+1;
  return posi+1;
  }
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],*pas_fini,*graf,*cate;
int nb,i;
char choix_affiche;

if ((argc==1)||((argc<4)&&(strcmp(argv[1],"-h"))))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <dico classe syntaxique> <dico groupe syntagme> \
 <nom arbre>\n",argv[0]);
 exit(0);
 }

if (!strcmp(argv[1],"-h"))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <dico classe syntaxique> <dico groupe syntagme> \
 <nom arbre> <type de sortie : 1 , 2 ou 3>\n\n\
 \t Programme de parsing d'un fichier .ecg envoye sur l'entree standard\n\
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

/*fprintf(stderr,"Chargement des lexiques -> ");*/
Lexique_Classe=ChargeLexique(argv[1]);
Lexique_Groupe=ChargeLexique(argv[2]);
/*fprintf(stderr,"Termine\n");*/
/*fprintf(stderr,"Chargement de l'arbre des syntagmes -> ");*/
pt_syntagme=charge_desc_syntagme(argv[3]);
/*fprintf(stderr,"Termine\n");*/

if (argc==5) choix_affiche=argv[4][0]; else choix_affiche='1';

pas_fini=fgets(ch,TailleLigne,stdin);
nb=0;
while (pas_fini)
 {
 graf=strtok(ch," \t\n");
 cate=strtok(NULL," \t\n");

 for (i=0;(graf[i])&&(i<TailleMaxMot-1);i++)
  if (graf[i]!='|') Phrase[nb][GRAF][i]=graf[i];
  else /* on vire les '|' pour eviter les confusions ds les syntagmes */
   Phrase[nb][GRAF][i]='/';

 Phrase[nb][GRAF][i]='\0';

 for (i=0;(cate[i])&&(i<TailleMaxMot-1);i++) Phrase[nb][CATE][i]=cate[i]; Phrase[nb][CATE][i]='\0';

 if ((!strcmp(graf,"</s>"))||(nb==MaxMotsParPhrase-2))
  {
  for (i=0;i<=nb;i=parse_chaine(i,0,choix_affiche));
  parse_chaine(nb,1,choix_affiche);
  if (choix_affiche=='3') printf("\n");
  nb=-1;
  }
 pas_fini=fgets(ch,TailleLigne,stdin);
 nb++;
 }
}
  
