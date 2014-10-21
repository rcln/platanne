/*  Extract proper-name from a POS tagged text corpus  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne	40000

#define True	1
#define False	0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

#define IF_MAJU(a)	((((a)>='A')&&((a)<='Z'))||((a)=='É')||((a)=='À')\
					||((a)=='È') ||((a)=='Ù') ||((a)=='Ê') ||((a)=='Î') ||((a)=='Ô')\
					||((a)=='Û') ||((a)=='Ä') ||((a)=='Ë') ||((a)=='Ï') ||((a)=='Ö')\
					||((a)=='Ü') ||((a)=='Ç'))

#define NON_ALPHA(a)	(((a)<'A')||(((a)>'Z')&&((a)<'a'))||((a)>'z'))

#define IF_PONCTU(a)	(((a)==',')||((a)=='.')||((a)==';')||((a)==':')||((a)=='!')||((a)=='?'))

#define IF_SEPAR(a)	(((a)=='-')||((a)=='_')||((a)=='&')||((a)=='*')||((a)=='@'))

#define TailleMot	200
#define WindowSize	10
#define ContextSize	4

char T_word[WindowSize][TailleMot];
char T_tagg[WindowSize][TailleMot];
char T_flag[WindowSize];

int sure_name(int i)
{
if ((T_tagg[i][0]=='X')||((!strcmp(T_tagg[i],"MOTINC"))&&(IF_MAJU(T_word[i][0])))) return True;
return False;
}

int find_name(int *indice)
{
int pafini,i;

for(i=ContextSize,pafini=True;(i<WindowSize)&&(pafini);)
 {
 if (sure_name(i)) pafini=True;
 else
  if ((i>ContextSize)&&(IF_SEPAR(T_word[i][0]))&&(i<WindowSize-1)&&(sure_name(i+1))) pafini=True;
  else
   if ((i>ContextSize)&&
		   ((!strcmp(T_word[i],"de"))||
			(!strcmp(T_word[i],"y"))||
			(!strcmp(T_word[i],"d'")))
		   &&(i<WindowSize-1)&&(sure_name(i+1)))
	   pafini=True;
   else pafini=False;
 if (pafini) i++;
 }
*indice=i;
return (i==ContextSize?False:True);
}

void print_name(int j)
{
int i;
for(i=ContextSize;i<j;i++)
 {
 if (i>ContextSize) printf(" ");
 printf("%s",T_word[i]);
 T_flag[i]=1;
 }
printf("\n");
}

void traite_ligne(char *ch, int if_cntx)
{
static char *lvide="  ";
char *word,*tagg;
int nb,i,j;

if (ch)
 {
 word=strtok(ch," \t\n");
 if (word) tagg=strtok(NULL," \t\n"); else tagg=NULL;
 if ((!word)||(!tagg)) { fprintf(stderr,"ERROR: bad format in input file line %d\n",nb+1); exit(0); }
 if (strlen(word)>=TailleMot) word[TailleMot-2]='\0';
 if (strlen(tagg)>=TailleMot) tagg[TailleMot-2]='\0';
 }
else tagg=word=lvide;

for(i=1;i<WindowSize;i++)
 {
 strcpy(T_word[i-1],T_word[i]);
 strcpy(T_tagg[i-1],T_tagg[i]);
 T_flag[i-1]=T_flag[i];
 }
strcpy(T_word[WindowSize-1],word);
strcpy(T_tagg[WindowSize-1],tagg);
T_flag[WindowSize-1]=0;

if (T_flag[ContextSize]==0)
 {
 if ((find_name(&j))&&((j>ContextSize+1)||(strlen(T_word[ContextSize])>1)))
  {
  if (if_cntx)
   {
   for (i=0;i<ContextSize;i++) { if (i>0) printf(" "); printf("%s",T_word[i]); }
   printf(" # "); print_name(j);
   for (i=1;i<ContextSize;i++) { if (i>1) printf(" "); printf("%s",T_word[i]); }
   printf(" # "); print_name(j);
   for (i=2;i<ContextSize;i++) { if (i>2) printf(" "); printf("%s",T_word[i]); }
   printf(" # "); print_name(j);
   for (i=3;i<ContextSize;i++) { if (i>3) printf(" "); printf("%s",T_word[i]); }
   printf(" # "); print_name(j);
   }
  else print_name(j);
  }
 }
}

int main(int argc, char **argv)
{
char ch[TailleLigne];
int nb,if_cntx;
if ((argc>1)&&(!strcmp(argv[1],"-cntx"))) if_cntx=True; else if_cntx=False;
for (nb=0;nb<WindowSize;nb++) T_flag[nb]=T_word[nb][0]=T_tagg[nb][0]='\0';
for(nb=0;fgets(ch,TailleLigne,stdin);nb++) traite_ligne(ch,if_cntx);
for(nb=0;nb<WindowSize;nb++) traite_ligne(NULL,if_cntx);
}
  
