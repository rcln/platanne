
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

int main(int argc, char **argv)
{
char ch[TailleLigne],orig[TailleLigne],*word,*cate,*comt,*lemm;
int nb,cutoff,total,t;

if (argc>1)
 {
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h]\n",argv[0]);
   exit(0);
   }
  else if (sscanf(argv[nb],"%d",&cutoff)!=1) ERREUR("bad value for cutoff:",argv[nb]);
 }
else { fprintf(stderr,"Syntax: %s <cutoff>\n",argv[0]); exit(0); }

for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 {
 strcpy(orig,ch);
 word=strtok(ch," \t\n");
 for(total=0,cate=strtok(NULL," \t\n");cate;cate=strtok(NULL," \t\n"))
  {
  comt=strtok(NULL," \t\n"); lemm=strtok(NULL," \t\n");
  if (sscanf(comt,"%d",&t)!=1) ERREUR("bad format:",orig);
  total+=t;
  }
 if (t>=cutoff) printf("%s",orig);
 }
}
 
