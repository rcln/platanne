/*  Filter ecg files in order to keep only some events  */
/*  FRED 0309  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne     8000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

int if_capital(char c)
{
if ((c>='A')&&(c<='Z')) return True;
if (
        (c=='É') ||
        (c=='À') ||
        (c=='È') ||
        (c=='Ù') ||
        (c=='Â') ||
        (c=='Ê') ||
        (c=='Î') ||
        (c=='Ô') ||
        (c=='Û') ||
        (c=='Ä') ||
        (c=='Ë') ||
        (c=='Ï') ||
        (c=='Ö') ||
        (c=='Ü') ||
        (c=='Ç')
   ) return True;
return False;
}

/*................................................................*/

char *T_cate[]={ "AMOUNT","FONC","LOC","ORG","PERS","PROD","TIME","" };

int main(int argc, char **argv)
{
char ch[TailleLigne],*ptword,*ptcate;
int nb;
/*
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-XXXX"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   XXXX
   }
  else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h]\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);
*/

for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 {
 /* we keep only XLOC XORG XPERS XPROD if the word starts with a capital letter */
 ptword=ptcate=NULL;
 ptword=strtok(ch," \t\n");
 if (ptword) ptcate=strtok(NULL," \t\n");
 if (ptcate)
  {
  if ((if_capital(ptword[0]))&&((!strcmp(ptcate,"XLOC"))||(!strcmp(ptcate,"XORG"))||(!strcmp(ptcate,"XPERS"))||(!strcmp(ptcate,"XPROD"))))
   printf("%s %s\n",ptword,ptcate);
  else
   if ((if_capital(ptword[0]))&&((!strcmp(ptcate,"LOC"))||(!strcmp(ptcate,"ORG"))||(!strcmp(ptcate,"PERS"))||(!strcmp(ptcate,"PROD"))))
    printf("%s X%s\n",ptword,ptcate);
  }
 }

exit(0);
}
 
