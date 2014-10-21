/*  Test if a word belong or not to a sirlex lexicon  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <libgram.h>

/*................................................................*/

#define TailleLigne	10000

#define True	1
#define False	0

#define max(a,b)        ((a)>(b)?(a):(b))
#define min(a,b)        ((a)<(b)?(a):(b))

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],*pt;
int nb,if_in;
wrd_index_t iwrd;
ty_lexique lexicon;

if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s <sirlex lexicon> [-in/-out]\n",argv[0]);
 exit(0);
 }

lexicon=ChargeLexique(argv[1]);

if ((argc>2)&&(!strcmp(argv[2],"-out"))) if_in=False; else if_in=True;

for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 {
 if ((nb+1)%100000==0) fprintf(stderr,"En cours : %d\n",nb+1);
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  if (Mot2Code(pt,&iwrd,lexicon))
   { if (if_in) printf("%s\n",pt); }
  else
   { if (if_in==False) printf("%s\n",pt); }
 }
}
 
