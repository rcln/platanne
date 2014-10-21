/*  Add a digit code to lexicon with phon: each different
 *  word token receive a different code. Warning the input
 *  lex MUST be sorted   */
/*  FRED 0703  */

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
char ch[TailleLigne],previous[TailleLigne],*word,*phon;
int nb;

for(nb=0,previous[0]='\0';fgets(ch,TailleLigne,stdin);)
 {
 word=strtok(ch,"\t"); if (word) phon=strtok(NULL,"\n"); else phon=NULL;
 if ((word==NULL)||(phon==NULL)) ERREUR("bad format:",ch);
 if (strcmp(word,previous)) nb++;
 printf("%d\t%s\t%s\n",nb,word,phon);
 strcpy(previous,word);
 }
}
  
