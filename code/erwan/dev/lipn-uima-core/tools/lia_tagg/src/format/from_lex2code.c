/*  Take a lexicon and encode it according to 2 dictionnaries:
 *   one on the words and one on the phones  */
/*  FRED 0704  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_tree_avl.h>

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

/* reading lexicon */

lia_avl_t read_lexicon(char *filename)
{
FILE *file;
static char ch[TailleLigne],*pt;
lia_avl_t resu;
int code;

if (!(file=fopen(filename,"rt"))) ERREUR("can't open:",filename);
for (resu=NULL;fgets(ch,TailleLigne,file);)
 {
 pt=strtok(ch," \t\n"); if (pt) pt=strtok(NULL," \t\n");
 if (pt)
  {
  if (sscanf(pt,"%d",&code)!=1) ERREUR("bad format in:",filename);
  }
 else ERREUR("bad format in:",filename);
 resu=lia_ajoute_element_code_avl(resu,ch,code,True,NULL);
 }
fclose(file);
return resu;
}

int word2code(lia_avl_t lexicon, char *word, int code_unk)
{
int code;
if (!lia_recherche_avl(lexicon,word,&code)) return code_unk;
return code;
}

/*................................................................*/


int main(int argc, char **argv)
{
char ch[TailleLigne],*pt;
int nb,code,debut;
lia_avl_t lexicon_word,lexicon_phon;

lexicon_word=lexicon_phon=NULL;

if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-lex_word"))
   {
   if (nb+1==argc) ERREUR("a value must follow parameter:",argv[nb]);
   lexicon_word=read_lexicon(argv[++nb]);
   } else
  if (!strcmp(argv[nb],"-lex_phon"))
   {
   if (nb+1==argc) ERREUR("a value must follow parameter:",argv[nb]);
   lexicon_phon=read_lexicon(argv[++nb]);
   } else
   {
   fprintf(stderr,"Syntax: %s [-h] -lex_word <lexicon> -lex_phon <lexicon>\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

if ((lexicon_word==NULL)||(lexicon_phon==NULL)) ERREUR("bad syntax, see '-h' option","");

for(nb=0;fgets(ch,TailleLigne,stdin);)
 {
 pt=strtok(ch,"\t"); if (pt==NULL) ERREUR("bad format in:",ch);
 if (!lia_recherche_avl(lexicon_word,pt,&code)) ERREUR("unknown word:",pt);
 printf("%d\t",code);
 for(debut=0,pt=strtok(NULL," \t\n");pt;pt=strtok(NULL," \t\n"),debut++)
  {
  if (debut) printf(" ");
  if (!lia_recherche_avl(lexicon_phon,pt,&code)) ERREUR("unknown phon:",pt);
  printf("%d",code);
  }
 printf("\n");
 }

exit(0);
}
 
