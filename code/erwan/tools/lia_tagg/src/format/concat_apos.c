/*  Concat sequences like: <w1> ' <w2> or <w1>' <w2>
 *  if the roken <w1>'<w2> exists in the lexicon  */
/*  FRED 0404  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_tree_avl.h>

/*................................................................*/

#define TailleLigne     40000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

/* reading lexicon */

lia_avl_t read_lexicon(char *filename)
{
FILE *file;
static char ch[TailleLigne],*pt;
lia_avl_t resu;

if (!(file=fopen(filename,"rt"))) ERREUR("can't open:",filename);
for (resu=NULL;fgets(ch,TailleLigne,file);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  resu=lia_ajoute_element_avl(resu,pt,True,NULL);
fclose(file);
return resu;
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],*pt,*ptbak1,*ptbak2,test[TailleLigne];
int nb;
lia_avl_t t_lexique;

if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s <lexicon>\n",argv[0]);
 exit(0);
 }

t_lexique=read_lexicon(argv[1]);
while(fgets(ch,TailleLigne,stdin))
 {
 ptbak2=ptbak1=NULL;
 for(pt=strtok(ch," \t\n");(ptbak2!=pt)||(ptbak1!=pt);)
  {
  if ((ptbak1)&&(ptbak1[strlen(ptbak1)-1]=='\''))
   {
   if ((ptbak2)&&(ptbak1[0]=='\''))
    {
    sprintf(test,"%s'%s",ptbak2,pt);
	/*fprintf(stderr,"test=[%s]\n",test);*/
    if (lia_recherche_avl(t_lexique,test,&nb))
     {
     printf(" %s",test);
	 /*fprintf(stderr,"test=[%s]\n",test);*/
	 /*
     ptbak2=strtok(NULL," \t\n");
     if (ptbak2) ptbak1=strtok(NULL," \t\n"); else ptbak1=NULL;
     if (ptbak1) pt=strtok(NULL," \t\n"); else pt=NULL;
	 */
	 ptbak2=NULL;
	 ptbak1=strtok(NULL," \t\n");
	 if (ptbak1) pt=strtok(NULL," \t\n"); else pt=NULL;
	 }
	else
     {
     sprintf(test,"%s'",ptbak2);
	 /*fprintf(stderr,"test=[%s]\n",test);*/
     if (lia_recherche_avl(t_lexique,test,&nb))
      {
      printf(" %s",test);
      ptbak2=NULL;
      ptbak1=pt;
      if (pt) pt=strtok(NULL," \t\n");
      }
     else
      {
      printf(" %s",ptbak2);
      ptbak2=ptbak1; ptbak1=pt; if (pt) pt=strtok(NULL," \t\n");
      }
     }
    }
   else
    {
    sprintf(test,"%s%s",ptbak1,pt);
	/*fprintf(stderr,"test=[%s]\n",test);*/
    if (lia_recherche_avl(t_lexique,test,&nb))
     {
     /*fprintf(stderr,"\tOK\n");*/
     if (ptbak2) printf(" %s",ptbak2);
     printf(" %s",test);
	 /*
     ptbak2=strtok(NULL," \t\n");
     if (ptbak2) ptbak1=strtok(NULL," \t\n"); else ptbak1=NULL;
     if (ptbak1) pt=strtok(NULL," \t\n"); else pt=NULL;
	 */
	 ptbak2=NULL;
	 ptbak1=strtok(NULL," \t\n");
	 if (ptbak1) pt=strtok(NULL," \t\n"); else pt=NULL;
     }
    else
     {
     if (ptbak2) printf(" %s",ptbak2);
     ptbak2=ptbak1; ptbak1=pt; if (pt) pt=strtok(NULL," \t\n");
     }
    }
   }
  else
   {
   if (ptbak2) printf(" %s",ptbak2);
   ptbak2=ptbak1; ptbak1=pt; if (pt) pt=strtok(NULL," \t\n");
   }
  }
 printf("\n");
 }
exit(0);
}
 
