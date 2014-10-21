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
/*  Compile un arbre de syntagme a partir d'un fichier
    de type : LEMONDE9293.cfg.lm3.vocab_compte , c'est a dire,
    sur chaque ligne : <decomposition>\t<syntagme>\t<compte>
    par exemple : PREPADE_DETFS_NFS       GP      356091
    Il faut aussi un dico des groupes syntaxiques compile au format ML
    et un dico des categories synt formant les decompositions au format ML
    La sortie est sous la forme de 3 fichiers :
    * <nom arbre>.syntree_des : description de la taille des tableaux
    * <nom arbre>.syntree_tab : arbre des decompositions codees dans un tableau
    * <nom arbre>.syntree_zon : zone des stockage des codes des groupes
				syntaxiques associes a chaque noeud  */
/*  FRED 0499  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#include <libgram.h>
#include <arbre_syntagme.h>

#define TailleLigne	200

/*................................................................*/

/*  Declaration des types codant l'arbre  */

typedef struct type_syntree
	{
	int c_code;		/* code de la cate syntaxique associe au noeud */
	short *tabsynt;		/* tableau des codes des groupes syntagmes contenant ce noeud */
	int nb_gs;		/* nombre de groupes syntagmes contenant ce noeud */
	struct type_syntree *fg,*fd;
	} *ty_syntree;

/* codage de la decomposition du syntagme a integrer */

typedef struct type_decosynt
	{
	int c_code;		/* code de la categorie syntaxique composant le syntagme */
	struct type_decosynt *suiv;
	} *ty_decosynt;

/*................................................................

declare dans <arbre_syntagme.h>

  Declaration des types permettant de stocker l'arbre ds un tableau  

typedef struct
	{
	int c_code;		 code de la cate syntaxique associe au noeud 
	int i_gs;		 indice ds le tableau de short codant les codes des groupes associes 
	int nb_gs;		 nombre de groupes syntagmes contenant ce noeud 
	int fg,fd;		 adresse ds le tableau des fg et fd 
	} type_syntab;

typedef struct type_desc_syntagme
	{
	type_syntab *tabl;
	short * zone;
	int t_tabl,t_zone;
	} *ty_desc_syntagme;

................................................................*/

/* quelques variables resolument globales */

ty_lexique Lexique_Classe,Lexique_Groupe; /* lexique des classe et des groupes de syntagmes */

int NbCodeGroupeSyntagme;	/* nombre de code de groupe syntagme ds le dico correspondant */

int NbNode_syntree;		/* nombre de noeud de l'arbre des deco de syntagmes */
type_syntab *Tabl_syntagme;	/* tableau stockant les noeuds des deco de syntagmes */

int NbTotalCodeGroupe;		/* nombre total de codes associes a l'ensemble des noeuds */
short *Zone_syntagme;		/* tableau des codes de groupes de syntagmes associes aux noeuds */

int I_tab=0,I_zon=0;		/* indice de recopie dans les tableaux */

/*................................................................*/

/* allocation memoire et tutti frutti */

ty_syntree new_ty_syntree(int c_code,int g_code,int si_fin_syntagme)
{
ty_syntree pt;
int n;
pt=(ty_syntree)malloc(sizeof(struct type_syntree));
pt->c_code=c_code;
pt->tabsynt=(short *)malloc(sizeof(short)*NbCodeGroupeSyntagme);
for(n=0;n<NbCodeGroupeSyntagme;n++) pt->tabsynt[n]=0;
/* si pt->tabsynt[g_code]=2 alors c'est une fin de syntagme, sinon pt->tabsynt[g_code]=1 */
if (si_fin_syntagme)  pt->tabsynt[g_code]=2; else pt->tabsynt[g_code]=1;
NbTotalCodeGroupe++;
pt->nb_gs=1;
pt->fg=pt->fd=NULL;
return pt;
}

void delete_ty_syntree(ty_syntree pt)
{
if (pt)
 {
 delete_ty_syntree(pt->fg);
 delete_ty_syntree(pt->fd);
 if (pt->tabsynt) free(pt->tabsynt);
 free(pt);
 }
}

ty_decosynt new_ty_decosynt(int c_code)
{
ty_decosynt pt;
pt=(ty_decosynt)malloc(sizeof(struct type_decosynt));
pt->c_code=c_code;
pt->suiv=NULL;
return pt;
}

void delete_ty_decosynt(ty_decosynt pt)
{
if (pt)
 {
 delete_ty_decosynt(pt->suiv);
 free(pt);
 }
}

/*................................................................*/

/* ajout d'element dans l'arbre syntree */

ty_syntree ajoute_syntree(ty_decosynt pt_deco,int g_code,ty_syntree pt_tree)
{
if (pt_deco==NULL) return pt_tree;

if (pt_tree==NULL)
 {
 NbNode_syntree++;
 pt_tree=new_ty_syntree(pt_deco->c_code,g_code,pt_deco->suiv==NULL?1:0);
 pt_tree->fg=ajoute_syntree(pt_deco->suiv,g_code,pt_tree->fg);
 return pt_tree;
 }

if (pt_tree->c_code==pt_deco->c_code)
 {
 if (pt_tree->tabsynt[g_code]==0)
  {
  pt_tree->tabsynt[g_code]=pt_deco->suiv==NULL?2:1;
  pt_tree->nb_gs++;
  NbTotalCodeGroupe++;
  }
 else
  if (pt_deco->suiv==NULL) pt_tree->tabsynt[g_code]=2;

 pt_tree->fg=ajoute_syntree(pt_deco->suiv,g_code,pt_tree->fg);
 return pt_tree;
 }

pt_tree->fd=ajoute_syntree(pt_deco,g_code,pt_tree->fd);
return pt_tree;
}

/*................................................................*/

/* construit une ty_decosynt a partir de la description en chaine de caractere */

ty_decosynt construit_decosynt(char *ch)
{
char *ch_code;
ty_decosynt racine,pt;
wrd_index_t c_code;

racine=pt=NULL;
for(ch_code=strtok(ch,"_");ch_code;ch_code=strtok(NULL,"_"))
 {
 if (!Mot2Code(ch_code,&c_code,Lexique_Classe))
  {
  fprintf(stderr,"Ben alors, c'est quoi cette classe : %s !!!!\n",ch_code);
  exit(0);
  }
 if (racine==NULL)
  racine=pt=new_ty_decosynt((int)c_code);
 else
  {
  pt->suiv=new_ty_decosynt((int)c_code);
  pt=pt->suiv;
  }
 }
return racine;
}

/*................................................................*/

/* Traitement du fichier de description, code groupe, compte */

ty_syntree construit_syntree(char *nomfich)
{
char ch[TailleLigne],*ch_deco,*ch_grpe;
FILE *file;
int nb;
wrd_index_t g_code;
ty_syntree racine;
ty_decosynt pt_deco;

if (!(file=fopen(nomfich,"rt")))
 {
 fprintf(stderr,"Can't open %s\n",nomfich);
 exit(0);
}
for(nb=0,racine=NULL;fgets(ch,TailleLigne,file);nb++)
 {
 if ((nb+1)%5000==0) fprintf(stderr,"en cours : %d\n",nb+1);
 ch_deco=strtok(ch," \t\n");
 ch_grpe=strtok(NULL," \t\n");
 
 pt_deco=construit_decosynt(ch_deco);

 if (!Mot2Code(ch_grpe,&g_code,Lexique_Groupe))
  {
  fprintf(stderr,"Ben alors, c'est quoi ce groupe : %s !!!!\n",ch_grpe);
  exit(0);
  }
 racine=ajoute_syntree(pt_deco,(int)g_code,racine);
 delete_ty_decosynt(pt_deco);
 }
fclose(file);

return racine;
}

/*................................................................*/

/* Ecriture de l'arbre dans un tableau et sauvegarde des infos */

int syntree_to_syntab(ty_syntree racine)
{
int n,i;

if ((I_tab>NbNode_syntree)||(I_zon>NbTotalCodeGroupe))
 {
 fprintf(stderr,"Et la !!!! NbNode_syntree=%d I_tab=%d  -  NbTotalCodeGroupe=%d I_zon=%d\n",
	NbNode_syntree,I_tab,NbTotalCodeGroupe,I_zon);
 exit(0);
 }

if (racine)
 {
 for(i=n=0;n<NbCodeGroupeSyntagme;n++)
  if (racine->tabsynt[n]==1)      Zone_syntagme[I_zon+i++]=n;
  else if (racine->tabsynt[n]==2) Zone_syntagme[I_zon+i++]=-n;

 if (i!=racine->nb_gs)
  {
  fprintf(stderr,"Zarbi : pourquoi il y a plus de code ds le tableau que dans racine->nb_gs (%d/%d)\n",
	i,racine->nb_gs);
  exit(0);
  }

 n=I_tab++;

 Tabl_syntagme[n].c_code=racine->c_code;
 Tabl_syntagme[n].i_gs=I_zon;
 I_zon+=i;
 Tabl_syntagme[n].nb_gs=racine->nb_gs;
 Tabl_syntagme[n].fg=syntree_to_syntab(racine->fg);
 Tabl_syntagme[n].fd=syntree_to_syntab(racine->fd);
 return n;
 }
else return -1;
}

/*................................................................*/

/* sauvegarde de l'arbre dans les tableaux */

void sauve_syntab(char *nomfich,char *fichtext)
{
char ch[TailleLigne];
FILE *file;

sprintf(ch,"%s.syntree_des",nomfich);
if (!(file=fopen(ch,"wt")))
 { fprintf(stderr,"Can't write in %s\n",ch); exit(0); }
fprintf(file,"%s\n%d %d\n",fichtext,NbNode_syntree,NbTotalCodeGroupe);
fclose(file);

sprintf(ch,"%s.syntree_tab",nomfich);
if (!(file=fopen(ch,"wb")))
 { fprintf(stderr,"Can't write in %s\n",ch); exit(0); }
fwrite(Tabl_syntagme,sizeof(type_syntab),NbNode_syntree,file);
fclose(file);

sprintf(ch,"%s.syntree_zon",nomfich);
if (!(file=fopen(ch,"wb")))
 { fprintf(stderr,"Can't write in %s\n",ch); exit(0); }
fwrite(Zone_syntagme,sizeof(short),NbTotalCodeGroupe,file);
fclose(file);
}

/*................................................................*/

int main(int argc, char **argv)
{
ty_syntree racine_syntree;

if ((argc==1)||((argc<=4)&&(strcmp(argv[1],"-h"))))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <dico classe syntaxique> <dico groupe syntagme> \
 <fich desc compte> <nom arbre>\n",argv[0]);
 exit(0);
 }

if (!strcmp(argv[1],"-h"))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <dico classe syntaxique> <dico groupe syntagme> \
 <fich desc compte> <nom arbre>\n\n\
 \t Compile un arbre de syntagme en partie commune avec info sur les codes.\n\
 \t Il faut, en entree :\n\
 \t  * <dico classe syntaxique> : dico compile au format ML des classes syntaxique composant les\n\
 \t                               syntagmes.\n\
 \t  * <dico groupe syntagme>   : dico compile au format ML des groupes de syntagmes.\n\
 \t  * <fich desc compte>       : fichier de description des syntagmes avec leurs comptes comme\n\
 \t                               dans le fichier 'LEMONDE9293.cfg.lm3.vocab_compte', c'est a dire\n\
 \t                               sur chaque ligne : <decomposition>\t<syntagme>\t<compte>\n\
 \t                               par exemple : PREPADE_DETFS_NFS  GP  356091\n\
 \t La sortie est sous la forme de 3 fichiers :\n\
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

NbCodeGroupeSyntagme=(int)Lexique_Groupe->GereLexique_NbMots;
NbNode_syntree=NbTotalCodeGroupe=I_tab=I_zon=0;

fprintf(stderr,"Traitement du fichier %s\n",argv[3]);
racine_syntree=construit_syntree(argv[3]);
fprintf(stderr,"Termine\n");

fprintf(stderr,"Allocation des tableaux necessaires au stockage de l'arbre -> ");
Tabl_syntagme=(type_syntab *)malloc(sizeof(type_syntab)*NbNode_syntree);
Zone_syntagme=(short *)malloc(sizeof(short)*NbTotalCodeGroupe);
fprintf(stderr,"Termine\n");

fprintf(stderr,"Taille necessaire a l'arbre : %.2f Ko (%d noeuds et %d shorts)\n",
	(float)(sizeof(type_syntab)*NbNode_syntree+sizeof(short)*NbTotalCodeGroupe)/(float)1024,
	NbNode_syntree,NbTotalCodeGroupe);

fprintf(stderr,"Stockage de l'arbre dans les tableaux -> ");
syntree_to_syntab(racine_syntree);
fprintf(stderr,"Termine\n");

if ((I_tab!=NbNode_syntree)||(I_zon!=NbTotalCodeGroupe))
 {
 fprintf(stderr,"WARNING : pourquoi I_tab=%d et NbNode_syntree=%d ou I_zon=%d et NbTotalCodeGroupe=%d\n",
	I_tab,NbNode_syntree,I_zon,NbTotalCodeGroupe);
 exit(0);
 }

fprintf(stderr,"Sauvegarde de l'arbre dans les tableaux -> ");
sauve_syntab(argv[4],argv[3]);
fprintf(stderr,"Termine\n");
}
 
