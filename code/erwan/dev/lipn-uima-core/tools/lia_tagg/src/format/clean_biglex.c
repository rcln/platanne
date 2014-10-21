/*  Take a biglex (lexunion format) and clean it,
 *  which means merge the lines with the same words and
 *  different POS tags, suppress the MOTINC tag when others
 *  are there  */
/*  FRED 0904  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne     4000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

/*  Bibliotheque de gestion d'arbres AVL  */
/*  FRED 1199  */
/*  MODIF 0600 : traitement des chaines de caracteres et compte des infos
 *  Modif 0703 : lia_recherche_avl return the node or NULL instead of 'int'
 *               a new function copying all the nodes of a tree in an array
 *               and sorting them according to their frequency
 *  Modif 0704 : the field 'nb' can also be a code associated to a word
 *  Modif 0904 : now the field info is generik  */


/*................................................................*/

/*  POS  */

char *Tabl_POS[]=
		{
		"ADV", "ADVNE", "ADVPAS", "AFP", "AFS", "AINDFP", "AINDFS", "AINDMP", "AINDMS", "AMP",
		"AMS", "CHIF", "COCO", "COSUB", "DETFP", "DETFS", "DETMP", "DETMS", "DINTFP", "DINTFS",
		"DINTMP", "DINTMS", "MOTINC", "NFP", "NFS", "NMP", "NMS", "PDEMFP", "PDEMFS", "PDEMMP",
		"PDEMMS", "PINDFP", "PINDFS", "PINDMP", "PINDMS", "PINTFP", "PINTFS", "PINTMP", "PINTMS",
		"PPER1P", "PPER1S", "PPER2P", "PPER2S", "PPER3FP", "PPER3FS", "PPER3MP", "PPER3MS",
		"PPOBJFP", "PPOBJFS", "PPOBJMP", "PPOBJMS", "PREFFP", "PREFFS", "PREFMP", "PREFMS",
		"PRELFP", "PRELFS", "PRELMP", "PRELMS", "PREP", "PREPADE", "PREPAU", "PREPAUX", "PREPDES",
		"PREPDU", "V1P", "V1S", "V2P", "V2S", "V3P", "V3S", "VA1P", "VA1S", "VA2P", "VA2S", "VA3P",
		"VA3S", "VAINF", "VE1P", "VE1S", "VE2P", "VE2S", "VE3P", "VE3S", "VEINF", "VINF", "VPPFP",
		"VPPFS", "VPPMP", "VPPMS", "VPPRE", "XFAMIL", "XPAYFP", "XPAYFS", "XPAYMP", "XPAYMS",
		"XPREF", "XPREM", "XSOC", "XVILLE", "YPFAI", "YPFOR", "ZTRM", ""
		};

int from_POS2code(char *ch)
{
int i;
for(i=0;(Tabl_POS[i][0])&&(strcmp(ch,Tabl_POS[i]));i++);
if (Tabl_POS[i][0]) return i+1;
else return 0;
}

char *from_code2POS(int code)
{
return Tabl_POS[code-1];
}

/*................................................................*/

#define LIA_MAX_TAILLE_MESSAGE	4000

int LIA_AVL_NB_NODE;

/*................................................................*/

#define MAX_POS	10

typedef struct
    {
    char *key_string;
	char l_POS[MAX_POS],*l_lemm[MAX_POS];
	int l_freq[MAX_POS];
    } type_info;

int compare_info(type_info *pt1, type_info *pt2)
{
return strcmp(pt1->key_string,pt2->key_string);
}

void print_info(type_info *pt)
{
int i;
printf("%s",pt->key_string);
for(i=0;(i<MAX_POS)&&(pt->l_POS[i]!=0);i++)
 if ((strcmp(from_code2POS(pt->l_POS[i]),"MOTINC"))||((i==0)&&(pt->l_POS[i+1]==0)))
  printf(" %s %d %s",from_code2POS(pt->l_POS[i]),pt->l_freq[i],pt->l_lemm[i]);
printf("\n");
}

type_info *new_type_info(char *key_string,char *desc)
{
type_info *pt;
char *item;
int i;
pt=(type_info *)malloc(sizeof(type_info));
if (key_string) pt->key_string=strdup(key_string); else key_string=NULL;
for(i=0,item=strtok(desc," \n\t");item;item=strtok(NULL," \n\t"),i++)
 {
 /* cat freq lemm */
 if (i==MAX_POS-1) ERREUR("cste MAX_POS too small for word:",key_string);
 pt->l_POS[i]=(char)from_POS2code(item);

 if (pt->l_POS[i]==0) ERREUR("POS unknown:",item);

 item=strtok(NULL," \n\t"); if (item==NULL) ERREUR("bad format1 in:",key_string);
 if (sscanf(item,"%d",&(pt->l_freq[i]))!=1) ERREUR("bad format2 in:",key_string);
 item=strtok(NULL," \n\t"); if (item==NULL) ERREUR("bad format3 in:",key_string);
 pt->l_lemm[i]=strdup(item);
 }
pt->l_POS[i]=0; pt->l_freq[i]=0; pt->l_lemm[i]=NULL;
return pt;
}

void add_type_info_from_string(type_info *info,char *key_string,char *desc)
{
char *item;
int i,freq,code;
for(item=strtok(desc," \n\t");item;item=strtok(NULL," \n\t"),i++)
 {
 /* cat freq lemm */
 code=from_POS2code(item);
 if (code==0) ERREUR("POS unknown:",item);
 for(i=0;(i<MAX_POS-1)&&(info->l_POS[i]!=0)&&(info->l_POS[i]!=(char)code);i++);
 if (i>=MAX_POS-1) ERREUR("cste MAX_POS too small for word:",key_string);

 if (info->l_POS[i]==0)
  {
  info->l_POS[i]=code; info->l_freq[i]=0; info->l_lemm[i]=NULL;
  info->l_POS[i+1]=0; info->l_freq[i+1]=0; info->l_lemm[i+1]=NULL;
  }
 
 item=strtok(NULL," \n\t"); if (item==NULL) ERREUR("bad4 format in:",key_string);
 if (sscanf(item,"%d",&(freq))!=1) ERREUR("bad format5 in:",key_string);

 info->l_freq[i]+=freq;
 
 item=strtok(NULL," \n\t"); if (item==NULL) ERREUR("bad format6 in:",key_string);
 if (info->l_lemm[i]==NULL) info->l_lemm[i]=strdup(item);
 }
}

void add_type_info(type_info *info1, type_info *info2) /* from info1 to info2 */
{
int n,i,freq,code;
for(n=0;(n<MAX_POS-1)&&(info1->l_POS[n]!=0);n++)
 {
 for(i=0;(i<MAX_POS-1)&&(info2->l_POS[i]!=0)&&(info2->l_POS[i]!=info1->l_POS[n]);i++);
 if (i>=MAX_POS-1) ERREUR("cste MAX_POS too small for word:",info1->key_string);

 if (info2->l_POS[i]==0)
  {
  info2->l_POS[i]=info1->l_POS[n];
  info2->l_freq[i]=info1->l_freq[n];
  info2->l_lemm[i]=strdup(info1->l_lemm[n]);

  info2->l_POS[i+1]=0; info2->l_freq[i+1]=0; info2->l_lemm[i+1]=NULL;
  }
 else
  info2->l_freq[i]+=info1->l_freq[n];
 }
}

void delete_type_info(type_info *pt)
{
int i;
for(i=0;(i<MAX_POS)&&(pt->l_POS[i]!=0);i++) if (pt->l_lemm[i]) free(pt->l_lemm[i]);
free(pt);
}

/*................................................................*/

/* declaration du type noeud des arbres AVL */

typedef struct lia_avl_type
	{
	signed char dq;
	type_info *info;
	struct lia_avl_type *fg,*fd;
	} *lia_avl_t;

/*................................................................*/

#define VERBOSE 0

/* les rotations */

/* parametres :
	1- lia_avl_t = racine de l'arbre */
/* retour : True=rotation effectuee / False=rotation impossible */
int lia_rotation_avl_droite(lia_avl_t);
int lia_rotation_avl_gauche(lia_avl_t);
int lia_rotation_avl_gauche_droite(lia_avl_t);
int lia_rotation_avl_droite_gauche(lia_avl_t);

/*................................................................*/

/* l'insertion d'un element */

/* parametres :
	1- lia_avl_t = racine de l'arbre
	2- char * = info a ajouter a l'arbre
	3- int = booleen (True ou False) avec True=ajout avec reequilibrage
	4- char * = chaine de caractere recevant la trace de l'ajout */
/* retour : lia_avl_t = racine de l'arbre modifie */
lia_avl_t lia_ajoute_element_avl(lia_avl_t, type_info *, int, char *);

/*................................................................*/

/* la liberation de la place memoire de l'arbre */

/* parametres :
	1- lia_avl_t = racine de l'arbre */
void lia_libere_avl(lia_avl_t);

/*................................................................*/

/* la recherche d'un element */

/* parametres :
	1- lia_avl_t = racine de l'arbre
	2- char * = info a rechercher dans l'arbre
	3- int * = nb d'occurence de la chaine */
/* retour : the node containing the info or NULL */
lia_avl_t lia_recherche_avl(lia_avl_t, type_info *);

/*................................................................*/

/* l'affichage de l'arbre */

/* parametres :
	1- lia_avl_t = racine de l'arbre a afficher */
void lia_affiche_avl(lia_avl_t);

void lia_affiche_avl_simple(lia_avl_t ,FILE *);
 
/*................................................................*/

/* copy all the nodes of a tree into an array and sort them
 * according to their frequency */

/* parameters:
 *  1- lia_avl_t = root of the tree to copy
 *  2- int * = return value containing the size of the array */
/* return : the adress of the table containing all the nodes sorted */

lia_avl_t *lia_avl_tree2table_freq(lia_avl_t , int *);

/*................................................................*/

/* binary search, according to the code (or freq) on the table of nodes:
 *  1- lia_avl_t = adress of the node table (obtained with lia_avl_tree2table_freq)
 *  2- int = size of the table (# of elements)
 *  3- int = code or freq looked for  */
lia_avl_t *lia_avl_code2word(lia_avl_t *, int, int);


/*................................................................*/

/*  Bibliotheque de gestion d'arbres AVL  */
/*  FRED 1199  -  Modif 0500 : info=char *
 *                Modif 0703 :  - lia_recherche_avl return
 *                			      the node or NULL instead of 'int'
								- a new function copying all the nodes
								  of a tree in an array and sorting them
								  according to their frequency
				  Modif 0704 :  - the field 'nb' can be used as a code
				  Modif 0904 :  - the field info is a pointer toward any structure  */

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

int lia_reequilibre_droite(lia_avl_t racine,char *mesg,char *si_modif) /* racine->dq=+2 */
{
char *r_noeud;

if (racine==NULL)
 { if (VERBOSE) sprintf(mesg,"ERREUR : rotation impossible : racine==NULL"); return False; }

if (racine->fg==NULL)
 {  if (VERBOSE) sprintf(mesg,"ERREUR : rotation droite impossible : [%s]->fg==NULL",racine->info->key_string); return False; }

r_noeud=racine->info->key_string;

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
  if (VERBOSE) sprintf(mesg,"ERREUR : rotation droite impossible sur le noeud [%s]",racine->info->key_string);
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
  if (VERBOSE) sprintf(mesg,"ERREUR : gauche-droite impossible sur le noeud [%s]",racine->info->key_string);
 }
return False;
}

int lia_reequilibre_gauche(lia_avl_t racine,char *mesg,char *si_modif) /* racine->dq=-2 */
{
char *r_noeud;

if (racine==NULL)
 { if (VERBOSE) sprintf(mesg,"ERREUR : rotation impossible : racine==NULL"); return False; }

if (racine->fd==NULL)
 {  if (VERBOSE) sprintf(mesg,"ERREUR : rotation gauche impossible : [%s]->fd==NULL",racine->info->key_string); return False; }

r_noeud=racine->info->key_string;

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
  if (VERBOSE) sprintf(mesg,"ERREUR : rotation gauche impossible sur le noeud [%s]",racine->info->key_string);
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
  if (VERBOSE) sprintf(mesg,"ERREUR : droite-gauche impossible sur le noeud [%s]",racine->info->key_string);
 }
return False;
}

/*................................................................*/

/* l'insertion d'un element */

lia_avl_t lia_insere_avl(lia_avl_t racine, type_info *info, char *si_augm,int avec_reequilibrage,char *mesg)
{
int comp;

if (racine==NULL) { *si_augm=1; return new_tree_mot_node(info); }

comp=compare_info(racine->info,info);

if (comp==0)
 {
 /* message -> le noeud est deja dans l'arbre */
 if (VERBOSE) sprintf(mesg,"noeud [%s] deja present",info->key_string);

 add_type_info(info,racine->info); /* from info1 to info2 */
 delete_type_info(info);

 *si_augm=0;
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

lia_avl_t lia_ajoute_element_avl(lia_avl_t racine, type_info *info, int avec_reequilibrage, char *mesg)
{
char si_augm;
if (VERBOSE) mesg[0]='\0';
return lia_insere_avl(racine,info,&si_augm,avec_reequilibrage,mesg);
}

/*................................................................*/

/* la liberation de la place memoire de l'arbre */

void lia_libere_avl(lia_avl_t racine)
{
if (racine)
 {
 lia_libere_avl(racine->fg);
 lia_libere_avl(racine->fd);
 delete_type_info(racine->info);
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

int main(int argc, char **argv)
{
char ch[TailleLigne],*key_string,*desc;
int nb;
lia_avl_t biglex;
type_info *info;

for(biglex=NULL,nb=0;fgets(ch,TailleLigne,stdin);nb++)
 {
 key_string=strtok(ch," \t\n");
 if (key_string) desc=strtok(NULL,"\n"); else desc=NULL;
 if ((key_string==NULL)||(desc==NULL)) ERREUR("bad format8 in:",ch);
 info=new_type_info(key_string,desc);
 biglex=lia_ajoute_element_avl(biglex,info,True,NULL);
 }
lia_affiche_avl_simple(biglex,stdout);

exit(0);
}
 
