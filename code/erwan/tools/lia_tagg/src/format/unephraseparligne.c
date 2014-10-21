#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

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

int main(int argc, char **argv)
{
char ch[TailleLigne],*pt;
int nb,remove_cc,valcut,if_fmt,prem;

remove_cc=if_fmt=False;
valcut=10000;

if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-remove_cc")) remove_cc=True; else
  if (!strcmp(argv[nb],"-fmt")) if_fmt=True; else
  if (!strcmp(argv[nb],"-cut"))
   {
   if (nb+1==argc) ERREUR("a value must follow option:",argv[nb]);
   if (sscanf(argv[++nb],"%d",&valcut)!=1) ERREUR("bad value:",argv[nb]);
   } else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] [-remove_cc] [-cut <int>] [-fmt]\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

for(prem=False,nb=0;fgets(ch,TailleLigne,stdin);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  {
  if (!strcmp(pt,"</s>"))
    {
    nb=0;
    if (remove_cc) printf("\n");
    else if (if_fmt) printf("\n</s>\n"); else printf(" </s>\n");
    }
  else
  if (!strcmp(pt,"<s>"))
   {
   if (!remove_cc) if (if_fmt) { prem=True; printf("<s>\n"); } else printf("<s>");
   }
  else
   {
   if (nb>=valcut)
    {
    nb=0;
    if (if_fmt) { printf("\n</s>\n<s>\n"); prem=True; }
    else printf("%s",remove_cc?"\n":" </s>\n<s>");
    }
   if ((if_fmt)&&(prem)) printf("%s",pt); else printf(" %s",pt);
   prem=False;
   nb++;
   }
  }

}
 
