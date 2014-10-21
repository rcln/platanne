/*  Take as input a ARPA file produced by SRILM and
 *  sort the ngram and add 0 backoff if necesary  */
/*  FRED 1104  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne     400

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

/* format: */

/*
\data\
ngram 1=62655
ngram 2=2958090
ngram 3=1903305

\1-grams:
-4.381225       's      -0.1422916
-5.095756       3D      -0.1802902
-1.726392       </s>    0
*/

typedef struct
	{
    char *w,*p;
	} type_ngram;

#define MAX_ORDER	4

type_ngram *new_type_ngram(char *p, char *w)
{
type_ngram *pt;
pt=(type_ngram *)malloc(sizeof(type_ngram));
if (p) pt->p=strdup(p); else pt->p=NULL;
if (w) pt->w=strdup(w); else pt->w=NULL;
return pt;
}

void delete_type_ngram(type_ngram *pt)
{
if (pt)
 {
 if (pt->p) free(pt->p);
 if (pt->w) free(pt->w);
 free(pt);
 }
}

int compare_type_ngram(const void *a, const void *b)
{
type_ngram **c,**d;
char *e,*f;
int i;
c=(type_ngram **)a;
d=(type_ngram **)b;
return strcmp((*c)->w,(*d)->w);
}

int compare_unk_type_ngram(const void *a, const void *b) /* we put <UNK> first */
{
type_ngram **c,**d;
char *e,*f;
int i;
c=(type_ngram **)a;
d=(type_ngram **)b;
e=(*c)->w;
f=(*d)->w;

for(i=0;e[i];i++)
 {
 if (f[i]=='\0') return 1;
 if (!strncmp(e+i,"<UNK>",5))
  {
  if (!strncmp(f+i,"<UNK>",5)) i+=4;
  else return -1;
  }
 else
  if (!strncmp(f+i,"<UNK>",5)) return 1;
  else
   if (e[i]!=f[i])
    {
    if ((e[i]==' ')||(e[i]=='\t'))
     if ((f[i]!=' ')&&(f[i]!='\t')) return -1;
	 else ;
    else
     if ((f[i]!=' ')&&(f[i]!='\t')) return e[i]-f[i];
	 else return 1;
	}
 }
if (f[i]=='\0') return 0;
return -1;
}

#define MAX_RM_TOKEN	20

int main(int argc, char **argv)
{
char ch[TailleLigne],*pt,ch2[TailleLigne],*p,*w,ch0[TailleLigne],*t_rmtoken[MAX_RM_TOKEN];
int nb,norder,t_size[MAX_ORDER],i,j,if_unk,nbrealngram;
type_ngram **t_gram;

for(i=0;i<MAX_RM_TOKEN;i++) t_rmtoken[i]=NULL;
if_unk=False;
norder=3;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-n"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   if (sscanf(argv[++nb],"%d",&norder)!=1) ERREUR("bad ngram order:",argv[nb]);
   }
  else
  if (!strcmp(argv[nb],"-unk"))
   {
   if_unk=True;
   }
  else
  if (!strcmp(argv[nb],"-rm"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   for(i=0;(i<MAX_RM_TOKEN-1)&&(t_rmtoken[i]);i++);
   if (i==MAX_RM_TOKEN-1) ERREUR("ctse 'MAX_RM_TOKEN' too small","");
   t_rmtoken[i]=strdup(argv[++nb]);
   }
  else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] -n <int> [-unk] [-rm <token1>] [-rm <token2>] ....\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

if (norder>MAX_ORDER) ERREUR("cste 'MAX_ORDER' too small","");
fprintf(stderr,"Procesing %d-gram model: (%s <UNK>)\n",norder,if_unk?"with":"without");
for(i=0;i<norder;i++) t_size[i]=-1;

for(;(fgets(ch,TailleLigne,stdin))&&(strncmp(ch,"\\data\\",6));) printf("%s",ch);
printf("%s",ch);
for(;(fgets(ch,TailleLigne,stdin))&&(strncmp(ch,"\\1-grams:",9));)
 {
 printf("%s",ch);
 if (sscanf(ch,"ngram %d=%d",&i,&nb)==2)
  if (i<=norder) t_size[i-1]=nb; else ERREUR("bad order value:",argv[2]);
 }
for(i=0;i<norder;i++) if (t_size[i]==-1) ERREUR("bad arpa input file (1)","");
printf("%s",ch);

for(nb=1;nb<=norder;nb++)
 {
 fprintf(stderr,"\t- %d-gram ->",nb);

 t_gram=(type_ngram **)malloc(sizeof(type_ngram *)*t_size[nb-1]);
 sprintf(ch2,"\\%d-grams:",nb+1);
 for(i=nbrealngram=0;(fgets(ch,TailleLigne,stdin))&&(strncmp(ch,ch2,9))&&(strncmp(ch,"\\end\\",5));)
  {
  p=strtok(ch," \t\n");
  if (p)
   {
   if (i>=t_size[nb-1])
    {
    fprintf(stderr,"ERREUR: nb theorique %d-gram=%d / nb lus=%d\n\tp=%s\n",nb,t_size[nb-1],i,p);
    exit(0);
	}
   for(ch0[0]='\0',w=strtok(NULL," \t\n");w;w=strtok(NULL," \t\n"))
    {
    if (ch0[0]) strcat(ch0,"\t");
	strcat(ch0,w);
	}
   if (ch0[0]=='\0') ERREUR("bad arpa input file (2)","");
   for(j=0;(t_rmtoken[j])&&(!strstr(ch0,t_rmtoken[j]));j++);
   if (t_rmtoken[j]==NULL) { t_gram[nbrealngram]=new_type_ngram(p,ch0); nbrealngram++; }
   i++;
   }
  }
 if (i<t_size[nb-1])
  {
  fprintf(stderr,"ERREUR: nb theorique %d-gram=%d / nb lus=%d\n",nb,t_size[nb-1],i);
  exit(0);
  }
 
 fprintf(stderr,"\nnb=%d / after filtering: nb=%d\n",t_size[nb-1],nbrealngram);
 t_size[nb-1]=nbrealngram;
 if (if_unk) qsort(t_gram,t_size[nb-1],sizeof(type_ngram *),compare_unk_type_ngram);
 else qsort(t_gram,t_size[nb-1],sizeof(type_ngram *),compare_type_ngram);
 for(i=0;i<t_size[nb-1];i++)
  {
  printf("%s",t_gram[i]->p);
  for(j=0,pt=strtok(t_gram[i]->w," \t\n");pt;pt=strtok(NULL," \t\n"),j++) printf("\t%s",pt);
  if ((nb<norder)&&(j==nb)) printf("\t0");
  printf("\n");
  delete_type_ngram(t_gram[i]);
  }
 free(t_gram);
 printf("\n");
 if (!feof(stdin)) printf("%s",ch);
 fprintf(stderr," done (new size = %d)\n",t_size[nb-1]);
 }
exit(0);
}
  
