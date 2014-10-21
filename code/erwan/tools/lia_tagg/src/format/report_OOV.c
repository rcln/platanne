/*  Print a report about OOV on a corpus according
 *  to a lexicon given as parameter */
/*  FRED 0703  */

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

if (!(file=fopen(filename,"rt"))) ERREUR("can't open:",filename);
for (resu=NULL;fgets(ch,TailleLigne,file);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  resu=lia_ajoute_element_avl(resu,pt,True,NULL);
fclose(file);
return resu;
}

/*................................................................*/

/*  checking each word of the corpus  */

lia_avl_t checking_corpus(FILE *file, lia_avl_t lexicon, int *nboov)
{
static char ch[TailleLigne],*pt;
lia_avl_t t_oov,resu;
int nb;

for (*nboov=0,t_oov=NULL;fgets(ch,TailleLigne,file);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  if (resu=lia_recherche_avl(lexicon,pt,&nb)) resu->nb++;
  else { t_oov=lia_ajoute_element_avl(t_oov,pt,True,NULL); (*nboov)++; }
  
return t_oov;
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne];
int nb,i,nboov;
lia_avl_t t_lexique, t_unknown,*table;

if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s <lexicon>\n",argv[0]);
 exit(0);
 }

t_lexique=read_lexicon(argv[1]);
t_unknown=checking_corpus(stdin,t_lexique,&nboov);
table=lia_avl_tree2table_freq(t_unknown,&nb);

printf("%d entree inconnues dans le corpus (%d occurrences)\n",nb,nboov);
for(i=0;i<nb;i++)
 printf("\t%5d \t %s\n",table[i]->nb,table[i]->info);

exit(0);
}
 
