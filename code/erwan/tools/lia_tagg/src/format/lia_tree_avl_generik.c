/*  Bibliotheque de gestion d'arbres AVL  */
/*  FRED 1199  -  Modif 0500 : info=char *
 *                Modif 0703 :  - lia_recherche_avl return
 *                			      the node or NULL instead of 'int'
								- a new function copying all the nodes
								  of a tree in an array and sorting them
								  according to their frequency
				  Modif 0704 :  - the field 'nb' can be used as a code
				  Modif 0904 :  - the field info is a pointer toward any structure  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <lia_tree_info.h>
#include <lia_tree_avl_generik.h>

#define TailleLigne	40000

int LIA_AVL_NB_NODE;

/*................................................................*/

/* les rotations */

int lia_rotation_avl_droite(lia_avl_t pt)
{
lia_avl_t tmpfgfd,tmpfd;
type_info *tmpinfo;
char tmpdq;

if ((pt==NULL)||(pt->fg==NULL)) return False; /* la rotation n'est pas definie */

/*  On echange pt et fg  */
tmpinfo=pt->info;
tmpdq=pt->dq;
pt->info=pt->fg->info;
pt->dq=pt->fg->dq;
pt->fg->info=tmpinfo;
pt->fg->dq=tmpdq;

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
type_info *tmpinfo;
char tmpdq;

if ((pt==NULL)||(pt->fd==NULL)) return False; /* la rotation n'est pas definie */

/*  On echange pt et fd  */
tmpinfo=pt->info;
tmpdq=pt->dq;
pt->info=pt->fd->info;
pt->dq=pt->fd->dq;
pt->fd->info=tmpinfo;
pt->fd->dq=tmpdq;

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

lia_avl_t new_tree_mot_node(type_info *info)
{
lia_avl_t pt;
pt=(lia_avl_t)malloc(sizeof(struct lia_avl_type));
pt->dq=0;
pt->info=info;
pt->fg=pt->fd=NULL;
LIA_AVL_NB_NODE++;
return pt;
}

/*................................................................*/

/* reequilibrage */

int lia_reequilibre_droite(lia_avl_t racine,char *si_modif) /* racine->dq=+2 */
{
if (racine==NULL) return False;
if (racine->fg==NULL) return False;

*si_modif=racine->fg->dq==0?0:1;

if (racine->fg->dq>=0) /* 0 ou +1 */
 {
 if (lia_rotation_avl_droite(racine))
  {
  if (racine->dq==1) racine->dq=racine->fd->dq=0; else { racine->dq=-1; racine->fd->dq=1; }
  return True;
  }
 }
else
 {
 if (lia_rotation_avl_gauche_droite(racine))
  {
  switch (racine->dq)
   {
   case  1 : racine->fg->dq=0; racine->fd->dq=-1; break;
   case -1 : racine->fg->dq=1; racine->fd->dq= 0; break;
   case  0 : racine->fg->dq=racine->fd->dq=0; break;
   }
  racine->dq=0;
  return True;
  }
 }
return False;
}

int lia_reequilibre_gauche(lia_avl_t racine,char *si_modif) /* racine->dq=-2 */
{
if (racine==NULL) return False;
if (racine->fd==NULL) return False;

*si_modif=racine->fd->dq==0?0:1;

if (racine->fd->dq<1) /* -1 ou 0 */
 {
 if (lia_rotation_avl_gauche(racine))
  {
  if (racine->dq==-1) racine->dq=racine->fg->dq=0; else { racine->dq=1; racine->fg->dq=-1; }
  return True;
  }
 }
else
 {
 if (lia_rotation_avl_droite_gauche(racine))
  {
  switch (racine->dq)
   {
   case  1 : racine->fd->dq=-1; racine->fg->dq= 0; break;
   case -1 : racine->fd->dq= 0; racine->fg->dq= 1; break;
   case  0 : racine->fg->dq=racine->fd->dq=0; break;
   }
  racine->dq=0;
  return True;
  }
 }
return False;
}

/*................................................................*/

/* l'insertion d'un element */

lia_avl_t lia_insere_avl(lia_avl_t racine, type_info *info, char *si_augm)
{
int comp;

if (racine==NULL) { *si_augm=1; return new_tree_mot_node(info); }

comp=compare_info(racine->info,info);

if (comp==0)
 {
 /* message -> le noeud est deja dans l'arbre */
 *si_augm=0;
 }
else
 if (comp>0)
  { /* sur le fils gauche */
  racine->fg=lia_insere_avl(racine->fg,info,si_augm);
  if (*si_augm)
   {
   if (racine->dq<0) *si_augm=0;
   racine->dq++;
   }
  /* eventuelle rotation */
  if (racine->dq==2)
   { lia_reequilibre_droite(racine,si_augm); *si_augm=0; }
  }
 else
  { /* sur le fils droit */
  racine->fd=lia_insere_avl(racine->fd,info,si_augm);
  if (*si_augm)
   {
   if (racine->dq>0) *si_augm=0;
   racine->dq--;
   }
  /* eventuelle rotation */
  if (racine->dq==-2)
   { lia_reequilibre_gauche(racine,si_augm); *si_augm=0; }
  }
return racine;
}

lia_avl_t lia_ajoute_element_avl(lia_avl_t racine, type_info *info)
{
char si_augm;
return lia_insere_avl(racine,info,&si_augm);
}

/*................................................................*/

/* la liberation de la place memoire de l'arbre */

void lia_libere_avl(lia_avl_t racine)
{
if (racine)
 {
 lia_libere_avl(racine->fg);
 lia_libere_avl(racine->fd);
 delete(racine->info);
 free(racine);
 }
}

/*................................................................*/

/* la recherche d'un element */

lia_avl_t lia_recherche_avl(lia_avl_t racine, type_info *info)
{
int comp;
if (racine==NULL) return NULL;
if ((comp=compare_info(racine->info,info))==0) return racine;
if (comp>0)  return lia_recherche_avl(racine->fg,info);
else         return lia_recherche_avl(racine->fd,info);
}

/*................................................................*/

/* l'affichage de l'arbre */

void lia_affiche_avl_simple(lia_avl_t racine,FILE *file)
{
if (racine)
 {
 lia_affiche_avl_simple(racine->fg,file);
 print_info(racine->info);
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
return compare_info((*d)->info,(*c)->info);
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
/*
lia_avl_t *lia_avl_code2word(lia_avl_t *tabl, int nb, int code)
{
struct lia_avl_type tkey;
lia_avl_t key,*resu;
tkey.info->code=new_type_info(code);
key=(lia_avl_t)(&tkey);
resu=bsearch(&key,tabl,nb,sizeof(lia_avl_t),compare_freq);
free(tkey.info->code);
return resu;
}
*/

/*................................................................*/
 
