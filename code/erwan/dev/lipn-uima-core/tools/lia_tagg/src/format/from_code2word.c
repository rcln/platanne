/*  Take a lexicon and a code corpus, output the words  */
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

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],*pt;
int nb,size,code,first;
lia_avl_t lexicon=NULL,*tabavl,*token;

lexicon=NULL;
first=False;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-first")) first=True; else
  if (!strcmp(argv[nb],"-lex"))
   {
   if (nb+1==argc) ERREUR("a value must follow parameter:",argv[nb]);
   lexicon=read_lexicon(argv[++nb]);
   } else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] -lex <lexicon> [-first]\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

if (lexicon==NULL) ERREUR("bad syntax, see '-h' option","");

tabavl=lia_avl_tree2table_freq(lexicon,&size);
for(nb=0;fgets(ch,TailleLigne,stdin);)
 if (first)
  {
  pt=strtok(ch," \t\n"); pt=strtok(NULL,"\n");
  if (sscanf(ch,"%d",&code)!=1) ERREUR("bad code:",ch);
  token=lia_avl_code2word(tabavl,size,code);
  if (token) printf("%s",(*token)->info); else ERREUR("unknown code:",ch);
  printf("\t%s\n",pt);
  }
 else
  for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
   {
   if (sscanf(pt,"%d",&code)!=1) ERREUR("bad code:",pt);
   token=lia_avl_code2word(tabavl,size,code);
   if (token) printf("%s\n",(*token)->info); else ERREUR("unknown code:",pt); 
   }

exit(0);
}
  
