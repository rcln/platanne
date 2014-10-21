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

char decapital(char c)
{
if ((c>='A')&&(c<='Z')) return (c+('a'-'A'));
if (c=='É') return 'é';
if (c=='À') return 'à';
if (c=='È') return 'è';
if (c=='Ù') return 'ù';
if (c=='Â') return 'â';
if (c=='Ê') return 'ê';
if (c=='Î') return 'î';
if (c=='Ô') return 'ô';
if (c=='Û') return 'û';
if (c=='Ä') return 'ä';
if (c=='Ë') return 'ë';
if (c=='Ï') return 'ï';
if (c=='Ö') return 'ö';
if (c=='Ü') return 'ü';
if (c=='Ç') return 'ç';
return c;
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne];
int i;
while(fgets(ch,TailleLigne,stdin)) for(i=0;ch[i];i++) printf("%c",decapital(ch[i]));
exit(0);
}
 
