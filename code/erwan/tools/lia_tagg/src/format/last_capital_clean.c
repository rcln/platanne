/*  Last cleaning process, after everything else has been
 *  done.
 *  - All the unknown words (acconding to a word list
 *    given as parameter) which have capital letters and for
 *    which an uncapital version exists in the word list are
 *    decapitalized. (and accent).
 *  - We check also if a form with the first
 *    letter as a capital and the rest decapitalized exists.
 *  - Also, the token 'M' followed by a word
 *    starting with a capital letter is replaced my the word
 *    Monsieur. (same thing for MM, Mme, Mgr, Mr).
 *  - Clean also all the token with 'strange' characters, like
 *    different from 'A-Z a-z 0-9 _ ' : delete the token if
 *    there's nothing else with it, or replace by <UNK> if
 *    it's mixed with other characters.
 *  - The single letters L D S J C N followed by a single '
 *    are turned into l' d' etc
 *  - The symbols '\' are replaced by '_'
 *  - The symbols '!' are suppressed  */
/*  FRED 1103  */

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

int if_lettre(char c)
{
if ((c>='A')&&(c<='Z')) return True;
if ((c>='a')&&(c<='z')) return True;
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
        (c=='Ç') ||
		(c=='é') ||
		(c=='à') ||
		(c=='è') ||
		(c=='ù') ||
		(c=='â') ||
		(c=='ê') ||
		(c=='î') ||
		(c=='ô') ||
		(c=='û') ||
		(c=='ä') ||
		(c=='ë') ||
		(c=='ï') ||
		(c=='ö') ||
		(c=='ü') ||
		(c=='ç')

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

char *decapital_string(char *pt)
{
int i;
for(i=0;pt[i];i++) pt[i]=decapital(pt[i]);
return pt;
}

int at_least_one(char *pt)
{
int i;
for(i=0;(pt[i])&&((pt[i]<'A')||(pt[i]>'Z'));i++);
return (pt[i]?True:False);
}

/*................................................................*/

int if_zarbi(char *pt, int *mixt)
{
int zarb;

for(*mixt=False,zarb=False;*pt;pt++)
 if (((*pt)!='_')&&((*pt)!='\'')&&((*pt)!='\\')&&((*pt)!='/')&&((*pt)!='+')&&((*pt)!='!')&&
     (!if_lettre(*pt))&&(!(IF_NUMBER((*pt)))))
  zarb=True;
 else *mixt=True;
return zarb;
}

/*................................................................*/

#define SIZE_LINE	80

void print_token(char *pt,int *taille)
{
int i;
if (*taille>0) { printf(" "); (*taille)++; }
for(i=0;pt[i];(*taille)++,i++)
 if (pt[i]!='!') printf("%c",pt[i]=='\\'?'_':pt[i]);
if (*taille>SIZE_LINE) { printf("\n"); *taille=0; }
}

int main(int argc, char **argv)
{
char ch[TailleLigne],deca[TailleLigne],*pt,previous_M[TailleLigne],previous_apos[TailleLigne];
lia_avl_t t_lexique;
int taille,nb,nomodif;

nomodif=False;

if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s <word list> [-nomodif]\n",argv[0]);
 exit(0);
 }

if ((argc>2)&&(!strcmp(argv[2],"-nomodif"))) nomodif=True;

t_lexique=read_lexicon(argv[1]);

for(taille=0,previous_M[0]=previous_apos[0]='\0';fgets(ch,TailleLigne,stdin);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  {
  if (previous_M[0])
   {
   if ((if_capital(pt[0]))&&(!nomodif))
    {
    if (!strcmp(previous_M,"M")) print_token("monsieur",&taille); else
    if (!strcmp(previous_M,"MM")) print_token("messieur",&taille); else
    if (!strcmp(previous_M,"Mme")) print_token("madame",&taille); else
    if (!strcmp(previous_M,"Mgr")) print_token("monseigneur",&taille); else
    if (!strcmp(previous_M,"Mr")) print_token("mister",&taille); else
		ERREUR("what's that ??",previous_M);
    }
   else print_token(previous_M,&taille);
   previous_M[0]='\0';
   }

  if (previous_apos[0])
   {
   if (!strcmp(pt,"'")) { printf(" %s'",decapital_string(previous_apos)); taille+=3; }
   else print_token(previous_apos,&taille);
   previous_apos[0]='\0';
   }

  if (!strcmp(pt,"'")) ; else		/* we remove these tokens */
  if (!strcmp(pt,"/")) ; else
  if (!strcmp(pt,"\\")) ; else
  if (!strcmp(pt,"</s>")) printf(" </s>\n"); else
  if (!strcmp(pt,"<s>")) { printf("<s>"); taille=4; } else
  if ((!strcmp(pt,"M"))||(!strcmp(pt,"Mme"))||(!strcmp(pt,"MM"))||(!strcmp(pt,"Mgr"))||(!strcmp(pt,"Mr")))
   strcpy(previous_M,pt); else
  if ((!strcmp(pt,"L"))||(!strcmp(pt,"D"))||(!strcmp(pt,"S"))||(!strcmp(pt,"J"))||(!strcmp(pt,"C"))||
      (!strcmp(pt,"N")))
   strcpy(previous_apos,pt);
  else
   {
   if (((nomodif)&&(pt==ch))||(lia_recherche_avl(t_lexique,pt,&nb))) print_token(pt,&taille);
   else
    {
    strcpy(deca,pt); decapital_string(deca);
	if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	else
     {
     strcpy(deca,pt);  decapital_string(deca+1);
	 if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	 else
      if (pt[0]=='E') /* check for accent forms */
       {
       deca[0]='é';
	   if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	   else
       {
       deca[0]='è';
	   if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	   else
       {
       deca[0]='ê';
	   if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	   else
       {
       deca[0]='É';
	   if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	   else
       {
       deca[0]='È';
	   if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	   else
       {
       deca[0]='Ê';
	   if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	   else
        if (if_zarbi(pt,&nb))
         {
         if (nb) print_token("<UNK>",&taille); 
		 }
	    else print_token(pt,&taille);
       } } } } } }
	  else
      if (if_zarbi(pt,&nb))
       {
       if (nb) print_token("<UNK>",&taille);
       }
      else print_token(pt,&taille);
	 }
	}
   }
  }
exit(0);
}
 
