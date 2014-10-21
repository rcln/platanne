
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
char ch[TailleLigne];
int nb,add_cc,i;

add_cc=False;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-add_cc"))
   {
   /*if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);*/
   add_cc=True;
   }
  else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h]\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 {
 if (add_cc) printf("<s> ");
 for(i=0;(ch[i])&&(ch[i]!='\n');i++)
  if (ch[i]=='<')
   {
   for(++i;(ch[i])&&(ch[i]!='\n')&&(ch[i]!='>');i++);
   if (ch[i]!='>') ERREUR("tag not finished:",ch);
   for(++i;(ch[i])&&(ch[i]!='\n')&&(ch[i]==' ');i++);
   i--;
   }
  else printf("%c",ch[i]);
 if (add_cc) printf(" </s>");
 printf("\n");
 }
exit(0);
}
  
