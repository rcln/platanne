/*  Return the intersection of 2 word lists  */
/*  FRED 1103  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_tree_avl.h>

/*................................................................*/

#define TailleLigne     80000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

#define IF_CAPITAL(a)	(((a)>='A')&&((a)<='Z'))
#define IF_NUMBER(a)	(((a)>='0')&&((a)<='9'))

/*................................................................*/

/* reading lexicon */

lia_avl_t read_lexicon(char *filename)
{
FILE *file;
static char ch[TailleLigne],*pt;
lia_avl_t resu;

if (!(file=fopen(filename,"rt"))) ERREUR("can't open:",filename);
for (resu=NULL;fgets(ch,TailleLigne,file);)
 {
 pt=strtok(ch," \t\n");
 if (pt) resu=lia_ajoute_element_avl(resu,pt,True,NULL);
 }
fclose(file);
return resu;
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],token[TailleLigne];
lia_avl_t t_lexique;
FILE *file;
int nb,i;

if (argc<=2)
 {
 fprintf(stderr,"Syntaxe : %s <word list1> <word list2> -e\n",argv[0]);
 exit(0);
 }

t_lexique=read_lexicon(argv[1]);
if (!(file=fopen(argv[2],"rt"))) ERREUR("can't open:",argv[2]);

while(fgets(ch,TailleLigne,file))
 {
 for(i=0;(ch[i])&&(ch[i]!='\n')&&(ch[i]!=' ')&&(ch[i]!='\t');i++) token[i]=ch[i];
 token[i]='\0';
 if (lia_recherche_avl(t_lexique,token,&nb))
  { if ((argc<4)||(strcmp(argv[3],"-e"))) printf("%s",ch); }
 else
  { if ((argc==4)&&(!strcmp(argv[3],"-e"))) printf("%s",ch); }
 }
fclose(file);
exit(0);
}
  
