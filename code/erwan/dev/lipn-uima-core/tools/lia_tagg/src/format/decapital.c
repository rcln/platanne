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
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�') ||
		(c=='�')
   ) return True;
return False;
}

char decapital(char c)
{
if ((c>='A')&&(c<='Z')) return (c+('a'-'A'));
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
if (c=='�') return '�';
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
 
