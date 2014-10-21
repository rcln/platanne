
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
int nb;

for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  {
  if (!strcmp(pt,"</s>")) printf(" </s>\n");
  else printf(" %s",pt);
  }
}
 
