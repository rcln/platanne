/*  Print a report about the coverage of a lexicon
 *  toward a corpus. Detail the OOV words as well as
 *  the zeroton and output the words frequency  */
/*  FRED 1003  */

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

lia_avl_t checking_corpus(FILE *file, lia_avl_t lexicon)
{
static char ch[TailleLigne],*pt;
lia_avl_t t_oov,resu;
int nb;
unsigned long total;

for (t_oov=NULL,total=0;fgets(ch,TailleLigne,file);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"),total++)
  {
  if (resu=lia_recherche_avl(lexicon,pt,&nb)) resu->nb++;
  else t_oov=lia_ajoute_element_avl(t_oov,pt,True,NULL);
  if (((total+1)%1000000)==0) fprintf(stderr,"\tdone: %ld M words\n",(unsigned long)((total+1)/1000000));
  }
return t_oov;
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne];
int nb,nbentries,i,j;
lia_avl_t t_lexique, t_unknown,*table;
FILE *file;
unsigned long total;

if (argc<=2)
 {
 fprintf(stderr,"Syntaxe : %s <lexicon> <name report>\n",argv[0]);
 exit(0);
 }

t_lexique=read_lexicon(argv[1]);
t_unknown=checking_corpus(stdin,t_lexique);

fprintf(stderr,"- report OOV words\n");
sprintf(ch,"%s.oov",argv[2]);
if (!(file=fopen(ch,"wt"))) ERREUR("can't write in :",ch);
table=lia_avl_tree2table_freq(t_unknown,&nb);
fprintf(stderr,"\t-> %d entree inconnues dans le corpus\n",nb);
for(i=0;i<nb;i++)
 fprintf(file,"\t%5d \t %s\n",table[i]->nb,table[i]->info);
fclose(file);
free(table);
fprintf(stderr,"done\n");

fprintf(stderr,"- report ZEROTON words\n");
sprintf(ch,"%s.zeroton",argv[2]);
if (!(file=fopen(ch,"wt"))) ERREUR("can't write in :",ch);
table=lia_avl_tree2table_freq(t_lexique,&nb);
for(i=j=0;i<nb;i++) if (table[i]->nb==1) { j++; fprintf(file,"%s\n",table[i]->info); }
fclose(file);
fprintf(stderr,"\t-> %d ZEROTON dans le corpus\n",j);
fprintf(stderr,"done\n");

fprintf(stderr,"- report words from the lexicon in the corpus\n");
sprintf(ch,"%s.occ",argv[2]);
if (!(file=fopen(ch,"wt"))) ERREUR("can't write in :",ch);
for(nbentries=0,i=0,total=0;i<nb;i++) if (table[i]->nb>1)
 { nbentries++; total+=(unsigned long)(table[i]->nb-1); fprintf(file,"\t%5d \t %s\n",table[i]->nb-1,table[i]->info); }
free(table);
fclose(file);
fprintf(stderr,"\t-> %ld occurences for %d entries\n",total,nbentries);
fprintf(stderr,"done\n");
exit(0);
}
 
