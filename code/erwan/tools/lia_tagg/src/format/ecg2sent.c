/*  From a .ecg file to a 1 sentence / line file  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne	400

#define True	1
#define False	0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],*pt;
int nb,nbcara;

for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 if (!strncmp(ch,"<s>",3))
  {
  if (nb) printf("\n");
  printf("<s>"); nbcara=3;
  }
 else
  {
  pt=strtok(ch," \t\n");
  if (nbcara!=0) { printf(" "); nbcara++; }
  printf("%s",pt);
  nbcara+=strlen(pt);
  if ((nbcara>80)&&(argc>1)&&(!strcmp(argv[1],"-cut"))) { nbcara=0; printf("\n"); }
  }
printf("\n");
}
 
