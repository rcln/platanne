/*  Bibliotheque de gestion d'arbres AVL  */
/*  FRED 1199  -  Modif 0500 : info=char *
 *                Modif 0703 :  - lia_recherche_avl return
 *                			      the node or NULL instead of 'int'
								- a new function copying all the nodes
								  of a tree in an array and sorting them
								  according to their frequency
				  Modif 0704 :  - the field 'nb' can be used as a code  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <lia_tree_avl.h>

#define VERBOSE	0

#define TailleLigne	40000

int LIA_AVL_NB_NODE;

/*................................................................*/

/* les rotations */

int lia_rotation_avl_droite(lia_avl_t pt)
{
lia_avl_t tmpfgfd,tmpfd;
char *tmpinfo;
char tmpdq;
int tmpnb;

if ((pt==NULL)||(pt->fg==NULL)) return False; /* la rotation n'est pas definie */

/*  On echange pt et fg  */
tmpinfo=pt->info;
tmpdq=pt->dq;
tmpnb=pt->nb;
pt->info=pt->fg->info;
pt->dq=pt->fg->dq;
pt->nb=pt->fg->nb;
pt->fg->info=tmpinfo;
pt->fg->dq=tmpdq;
pt->fg->nb=tmpnb;

tmpfgfd=pt->fg->fd;
tmpfd=pt->fd;

pt->fd=pt->fg;
pt->fg=pt->fg->fg;
pt->fd->fg=tmpfgfd;
pt->fd->fd=tmpfd;

return True;
}

int lia_rotation_avl_gauche(lia_avl_t pt)
{
lia_avl_t tmpfdfg,tmpfg;
char *tmpinfo;
char tmpdq;
int tmpnb;

if ((pt==NULL)||(pt->fd==NULL)) return False; /* la rotation n'est pas definie */

/*  On echange pt et fd  */
tmpinfo=pt->info;
tmpdq=pt->dq;
tmpnb=pt->nb;
pt->info=pt->fd->info;
pt->dq=pt->fd->dq;
pt->nb=pt->fd->nb;
pt->fd->info=tmpinfo;
pt->fd->dq=tmpdq;
pt->fd->nb=tmpnb;

tmpfdfg=pt->fd->fg;
tmpfg=pt->fg;

pt->fg=pt->fd;
pt->fd=pt->fd->fd;
pt->fg->fd=tmpfdfg;
pt->fg->fg=tmpfg;

return True;
}

int lia_rotation_avl_gauche_droite(lia_avl_t pt)
{
return ((lia_rotation_avl_gauche(pt->fg))&&(lia_rotation_avl_droite(pt)))?True:False;
}

int lia_rotation_avl_droite_gauche(lia_avl_t pt)
{
return ((lia_rotation_avl_droite(pt->fd))&&(lia_rotation_avl_gauche(pt)))?True:False;
}

/*................................................................*/

/* la creation d'un noeud */

lia_avl_t new_tree_mot_node(char *info, int nb)
{
lia_avl_t pt;
pt=(lia_avl_t)malloc(sizeof(struct lia_avl_type));
pt->dq=0;
pt->nb=nb;
pt->info=(char*)strdup(info);
pt->fg=pt->fd=NULL;
LIA_AVL_NB_NODE++;
return pt;
}

/*................................................................*/

/* reequilibrage */

int lia_reequilibre_droite(lia_avl_t racine,char *mesg,char *si_modif) /* racine->dq=+2 */
{
char *r_noeud;

if (racine==NULL)
 { if (VERBOSE) sprintf(mesg,"ERREUR : rotation impossible : racine==NULL"); return False; }

if (racine->fg==NULL)
 {  if (VERBOSE) sprintf(mesg,"ERREUR : rotation droite impossible : [%s]->fg==NULL",racine->info); return False; }

r_noeud=racine->info;

*si_modif=racine->fg->dq==0?0:1;

if (racine->fg->dq>=0) /* 0 ou +1 */
 {
 if (lia_rotation_avl_droite(racine))
  {
  if (VERBOSE) sprintf(mesg+strlen(mesg)," rotation droite sur le noeud [%s]",r_noeud);
  if (racine->dq==1) racine->dq=racine->fd->dq=0; else { racine->dq=-1; racine->fd->dq=1; }
  return True;
  }
 else
  if (VERBOSE) sprintf(mesg,"ERREUR : rotation droite impossible sur le noeud [%s]",racine->info);
 }
else
 {
 if (lia_rotation_avl_gauche_droite(racine))
  {
  if (VERBOSE) sprintf(mesg+strlen(mesg)," rotation gauche-droite sur le noeud [%s]",r_noeud);
  switch (racine->dq)
   {
   case  1 : racine->fg->dq=0; racine->fd->dq=-1; break;
   case -1 : racine->fg->dq=1; racine->fd->dq= 0; break;
   case  0 : racine->fg->dq=racine->fd->dq=0; break;
   }
  racine->dq=0;
  return True;
  }
 else
  if (VERBOSE) sprintf(mesg,"ERREUR : gauche-droite impossible sur le noeud [%s]",racine->info);
 }
return False;
}

int lia_reequilibre_gauche(lia_avl_t racine,char *mesg,char *si_modif) /* racine->dq=-2 */
{
char *r_noeud;

if (racine==NULL)
 { if (VERBOSE) sprintf(mesg,"ERREUR : rotation impossible : racine==NULL"); return False; }

if (racine->fd==NULL)
 {  if (VERBOSE) sprintf(mesg,"ERREUR : rotation gauche impossible : [%s]->fd==NULL",racine->info); return False; }

r_noeud=racine->info;

*si_modif=racine->fd->dq==0?0:1;

if (racine->fd->dq<1) /* -1 ou 0 */
 {
 if (lia_rotation_avl_gauche(racine))
  {
  if (VERBOSE) sprintf(mesg+strlen(mesg)," rotation gauche sur le noeud [%s]",r_noeud);
  if (racine->dq==-1) racine->dq=racine->fg->dq=0; else { racine->dq=1; racine->fg->dq=-1; }
  return True;
  }
 else
  if (VERBOSE) sprintf(mesg,"ERREUR : rotation gauche impossible sur le noeud [%s]",racine->info);
 }
else
 {
 if (lia_rotation_avl_droite_gauche(racine))
  {
  if (VERBOSE) sprintf(mesg+strlen(mesg)," rotation droite-gauche sur le noeud [%s]",r_noeud);
  switch (racine->dq)
   {
   case  1 : racine->fd->dq=-1; racine->fg->dq= 0; break;
   case -1 : racine->fd->dq= 0; racine->fg->dq= 1; break;
   case  0 : racine->fg->dq=racine->fd->dq=0; break;
   }
  racine->dq=0;
  return True;
  }
 else
  if (VERBOSE) sprintf(mesg,"ERREUR : droite-gauche impossible sur le noeud [%s]",racine->info);
 }
return False;
}

/*................................................................*/

/* l'insertion d'un element */

lia_avl_t lia_insere_avl(lia_avl_t racine, char *info, char *si_augm,int avec_reequilibrage,char *mesg)
{
int comp;

if (racine==NULL) { *si_augm=1; return new_tree_mot_node(info,1); }

comp=strcmp(racine->info,info);

if (comp==0)
 {
 /* message -> le noeud est deja dans l'arbre */
 if (VERBOSE) sprintf(mesg,"noeud [%s] deja present",info);
 *si_augm=0;
 racine->nb++;
 }
else
 if (comp>0)
  { /* sur le fils gauche */
  racine->fg=lia_insere_avl(racine->fg,info,si_augm,avec_reequilibrage,mesg);
  if (*si_augm)
   {
   if (racine->dq<0) *si_augm=0;
   racine->dq++;
   }
  /* eventuelle rotation */
  if ((avec_reequilibrage)&&(racine->dq==2))
   { lia_reequilibre_droite(racine,mesg,si_augm); *si_augm=0; }
  }
 else
  { /* sur le fils droit */
  racine->fd=lia_insere_avl(racine->fd,info,si_augm,avec_reequilibrage,mesg);
  if (*si_augm)
   {
   if (racine->dq>0) *si_augm=0;
   racine->dq--;
   }
  /* eventuelle rotation */
  if ((avec_reequilibrage)&&(racine->dq==-2))
   { lia_reequilibre_gauche(racine,mesg,si_augm); *si_augm=0; }
  }
return racine;
}

lia_avl_t lia_insere_code_avl(lia_avl_t racine, char *info, int code, char *si_augm,int avec_reequilibrage,char *mesg)
{
int comp;

if (racine==NULL) { *si_augm=1; return new_tree_mot_node(info,code); }

comp=strcmp(racine->info,info);

if (comp==0)
 {
 /* message -> le noeud est deja dans l'arbre */
 if (VERBOSE) sprintf(mesg,"noeud [%s] deja present",info);
 *si_augm=0;
 if (racine->nb!=code) ERREUR("mismatch between word and code on:",info);
 }
else
 if (comp>0)
  { /* sur le fils gauche */
  racine->fg=lia_insere_code_avl(racine->fg,info,code,si_augm,avec_reequilibrage,mesg);
  if (*si_augm)
   {
   if (racine->dq<0) *si_augm=0;
   racine->dq++;
   }
  /* eventuelle rotation */
  if ((avec_reequilibrage)&&(racine->dq==2))
   { lia_reequilibre_droite(racine,mesg,si_augm); *si_augm=0; }
  }
 else
  { /* sur le fils droit */
  racine->fd=lia_insere_code_avl(racine->fd,info,code,si_augm,avec_reequilibrage,mesg);
  if (*si_augm)
   {
   if (racine->dq>0) *si_augm=0;
   racine->dq--;
   }
  /* eventuelle rotation */
  if ((avec_reequilibrage)&&(racine->dq==-2))
   { lia_reequilibre_gauche(racine,mesg,si_augm); *si_augm=0; }
  }
return racine;
}

lia_avl_t lia_ajoute_element_avl(lia_avl_t racine, char *info, int avec_reequilibrage, char *mesg)
{
char si_augm;
if (VERBOSE) mesg[0]='\0';
return lia_insere_avl(racine,info,&si_augm,avec_reequilibrage,mesg);
}

lia_avl_t lia_ajoute_element_code_avl(lia_avl_t racine, char *info, int code, int avec_reequilibrage, char *mesg)
{
char si_augm;
if (VERBOSE) mesg[0]='\0';
return lia_insere_code_avl(racine,info,code,&si_augm,avec_reequilibrage,mesg);
}

/*................................................................*/

/* la suppression d'un element */

lia_avl_t lia_delete_max(lia_avl_t racine, char *info, char *si_dimi,int avec_reequilibrage,char *mesg)
{
lia_avl_t tmp;

if (racine->fd==NULL)
 {
 strcpy(info,racine->info);
 tmp=racine->fg;
 free(racine->info);
 free(racine);
 *si_dimi=1;
 racine=tmp;
 }
else
 {
 racine->fd=lia_delete_max(racine->fd,info,si_dimi,avec_reequilibrage,mesg);
 if (*si_dimi)
  {
  if (racine->dq>=0) *si_dimi=0;
  racine->dq++;
  }
 /* eventuelle rotation */
 if ((avec_reequilibrage)&&(racine->dq==2)) lia_reequilibre_droite(racine,mesg,si_dimi);
 }
return racine;
}

lia_avl_t lia_delete_avl(lia_avl_t racine, char *info, char *si_dimi,int avec_reequilibrage,char *mesg)
{
lia_avl_t tmp;
char *sauv_noeud;
static char temp_info[TailleLigne];
int comp;

if (racine==NULL) { *si_dimi=0; if (VERBOSE) sprintf(mesg,"noeud [%s] absent de l'arbre",info); }
else
 if ((comp=strcmp(racine->info,info))==0)
  {
  if ((racine->fg==NULL)&&(racine->fd==NULL))
   {
   /* c'est une feuille */
   free(racine);
   *si_dimi=1;
   racine=NULL;
   }
  else
   if ((racine->fg==NULL)||(racine->fd==NULL))
    {
    /* c'est un noeud simple */
    tmp=racine->fg?racine->fg:racine->fd;
    free(racine);
    *si_dimi=1;
    racine=tmp;
    }
   else
    {
    /* c'est un noeud double */
    sauv_noeud=racine->info;
    racine->fg=lia_delete_max(racine->fg,temp_info,si_dimi,avec_reequilibrage,mesg);
    racine->info=(char*)strdup(temp_info);
    if (VERBOSE) sprintf(mesg+strlen(mesg)," on echange le noeud [%s] et [%s]",sauv_noeud,racine->info);
    free(sauv_noeud);
    if (*si_dimi)
     {
     if (racine->dq<=0) *si_dimi=0;
     racine->dq--;
     }
    /* eventuelle rotation */
    if ((avec_reequilibrage)&&(racine->dq==-2)) lia_reequilibre_gauche(racine,mesg,si_dimi);
    }
  }
 else
  if (comp>0)
   { /* sur le fils gauche */
   racine->fg=lia_delete_avl(racine->fg,info,si_dimi,avec_reequilibrage,mesg);
   if (*si_dimi)
    {
    if (racine->dq<=0) *si_dimi=0;
    racine->dq--;
    }
   /* eventuelle rotation */
   if ((avec_reequilibrage)&&(racine->dq==-2)) lia_reequilibre_gauche(racine,mesg,si_dimi);
   }
  else
   { /* sur le fils droit */
   racine->fd=lia_delete_avl(racine->fd,info,si_dimi,avec_reequilibrage,mesg);
   if (*si_dimi)
    {
    if (racine->dq>=0) *si_dimi=0;
    racine->dq++;
    }
   /* eventuelle rotation */
   if ((avec_reequilibrage)&&(racine->dq==2)) lia_reequilibre_droite(racine,mesg,si_dimi);
   }
return racine;
}

lia_avl_t lia_supprime_element_avl(lia_avl_t racine, char *info, int avec_reequilibrage, char *mesg)
{
char si_augm;
if (VERBOSE) mesg[0]='\0';
return lia_delete_avl(racine,info,&si_augm,avec_reequilibrage, mesg);
}

/*................................................................*/

/* la liberation de la place memoire de l'arbre */

void lia_libere_avl(lia_avl_t racine)
{
if (racine)
 {
 lia_libere_avl(racine->fg);
 lia_libere_avl(racine->fd);
 free(racine);
 }
}

/*................................................................*/

/* la recherche d'un element */

lia_avl_t lia_recherche_avl(lia_avl_t racine, char *info,int *nb)
{
int comp;
if (racine==NULL) return NULL;
if ((comp=strcmp(racine->info,info))==0) { *nb=racine->nb; return racine; }
if (comp>0)  return lia_recherche_avl(racine->fg,info,nb);
else         return lia_recherche_avl(racine->fd,info,nb);
}

/*................................................................*/

/* l'affichage de l'arbre */

void lia_affiche_avl(lia_avl_t racine)
{
if (racine)
 {
 printf("noeud [%s] avec dq=%d et ",racine->info,racine->dq);
 if (racine->fg) printf("fg=[%s] , ",racine->fg->info); else printf("fg=NULL , ");
 if (racine->fd) printf("fd=[%s]\n",racine->fd->info); else printf("fd=NULL\n");
 lia_affiche_avl(racine->fg);
 lia_affiche_avl(racine->fd);
 }
}

void lia_affiche_avl_simple(lia_avl_t racine,FILE *file)
{
if (racine)
 {
 lia_affiche_avl_simple(racine->fg,file);
 fprintf(file,"%5d\t%s\n",racine->nb,racine->info);
 lia_affiche_avl_simple(racine->fd,file);
 }

}
  
/*................................................................*/

/* copy all the nodes of a tree into an array and sort them
 * according to their frequency */

int compare_freq(const void *a, const void *b)
{
lia_avl_t *c,*d;
c=(lia_avl_t *)a;
d=(lia_avl_t *)b;
return ((*d)->nb - (*c)->nb);
}

void copy_tree2table(lia_avl_t racine, lia_avl_t *tabl, int *i)
{
if (racine!=NULL)
 {
 tabl[(*i)++]=racine;
 copy_tree2table(racine->fg,tabl,i);
 copy_tree2table(racine->fd,tabl,i);
 }
}

int lia_avl_size(lia_avl_t racine)
{
if (racine==NULL) return 0;
else return 1 + lia_avl_size(racine->fg) + lia_avl_size(racine->fd);
}

lia_avl_t *lia_avl_tree2table_freq(lia_avl_t racine, int *nb)
{
lia_avl_t *tabl;
int i;
*nb=lia_avl_size(racine);
tabl=(lia_avl_t *)malloc(sizeof(lia_avl_t)*(*nb));
i=0;
copy_tree2table(racine,tabl,&i);
qsort(tabl,*nb,sizeof(lia_avl_t),compare_freq);
return tabl;
}

lia_avl_t *lia_avl_code2word(lia_avl_t *tabl, int nb, int code)
{
struct lia_avl_type tkey;
lia_avl_t key;
tkey.nb=code;
key=(lia_avl_t)(&tkey);
return bsearch(&key,tabl,nb,sizeof(lia_avl_t),compare_freq);
}

/*................................................................*/
 
