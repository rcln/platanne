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
int nb,if_sent,if_word,if_ben;

if_sent=if_word=if_ben=False;

if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] [-sent/-word]]\n",argv[0]);
   exit(0);
   } else
  if (!strcmp(argv[nb],"-sent")) if_sent=True; else
  if (!strcmp(argv[nb],"-word")) if_word=True; else
  if (!strcmp(argv[nb],"-ben")) if_ben=True;
  else ERREUR("unknown option:",argv[nb]);

if (if_sent)
 for(;fgets(ch,TailleLigne,stdin);)
  for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
   {
   if ((if_ben)&&(pt[0]=='<')&&(pt[strlen(pt)-1]=='>')) printf(" %s",pt);
   else
    if (!strcmp(pt,"</s>")) printf(" </s>\n"); else
    if (!strcmp(pt,"<s>")) printf("<s>");
    else { printf(" "); for(nb=0;pt[nb];nb++) printf("%c",((pt[nb]!='_')&&(pt[nb]!='-'))?pt[nb]:' '); }
   }
else

if (if_word)
 {
 for(;fgets(ch,TailleLigne,stdin);)
  for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
   {
   if ((if_ben)&&(pt[0]=='<')&&(pt[strlen(pt)-1]=='>')) printf("%s\n",pt);
   else
    {
	for(nb=0;pt[nb];nb++)
	 if ((pt[nb]!='_')&&(pt[nb]!='-')) printf("%c",pt[nb]);
	 else if ((pt[nb+1]!='_')&&(pt[nb+1]!='-')) printf("\n");
    if ((pt[nb-1]!='_')&&(pt[nb-1]!='-')) printf("\n");
	}
   }
 }
else
 for(;fgets(ch,TailleLigne,stdin);)
  for(nb=0;ch[nb];nb++)
   if ((if_ben)&&(ch[nb]=='<')&&(strstr(ch+nb+1,">")))
    {
    for(;(ch[nb])&&(ch[nb]!='>');nb++) printf("%c",ch[nb]);
	printf("%c",ch[nb]);
	}
   else printf("%c",((ch[nb]!='_')&&(ch[nb]!='-'))?ch[nb]:' ');
exit(0);
}
  
