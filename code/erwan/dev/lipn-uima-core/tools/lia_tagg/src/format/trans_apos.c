/*  Change from 'a to '_a and vice-versa  */
/*  FRED 0904  */

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
int nb,glue,i,debut,space;

glue=True; space=False;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-glue")) glue=True; else
  if (!strcmp(argv[nb],"-deglue")) glue=False; else
  if (!strcmp(argv[nb],"-space")) space=True; else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] [-glue/-deglue] [-space]\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);
for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 {
 debut=True;
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  {
  if (debut) debut=False; else printf(" ");
  for(i=0;pt[i];i++)
   if (pt[i]=='\'')
    {
    printf("'");
    if (glue)
     {
     if (pt[i+1]=='_') i++;
	 }
    else
     {
     if (pt[i+1]) printf("%c",space?' ':'_');
  	 }
	}
   else printf("%c",pt[i]);
  }
 printf("\n");
 }
exit(0);
}
  
