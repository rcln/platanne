/*  Remove the first capital words of the begining of every sentence
 *  already cut by <s> and </s> symbols. The rules are:
 *   - if the word with no capital exists in a list of words given
 *     as parameter, we remove the capital letters
 *   - if not, if the word starts with an 'E' and is not followed
 *     by a number, we try e1, e2 ,e3 in the same list of words
 *   - if not but there's a ' inside the word we check if the
 *     form stopping at ' exists in the lexicon  */
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

char *decapital(char *pt)
{
int i;
for(i=0;pt[i];i++)
 if ((pt[i]>='A')&&(pt[i]<='Z')) pt[i]+='a'-'A';
return pt;
}

int at_least_one(char *pt)
{
int i;
for(i=0;(pt[i])&&((pt[i]<'A')||(pt[i]>'Z'));i++);
return (pt[i]?True:False);
}

int all_capital(char *pt)
{
int i;
for(i=0;(pt[i])&&(pt[i]>='A')&&(pt[i]<='Z');i++);
return (pt[i]?False:True);
}

int inside_apos(char *pt, int *posi)
{
for(*posi=0;(pt[*posi])&&(pt[*posi]!='\'');(*posi)++);
return pt[*posi]?True:False;
}

/*................................................................*/

#define SIZE_LINE	80

void print_token(char *pt,int *taille)
{
if (*taille>0) { printf(" "); (*taille)++; }
printf("%s",pt); (*taille)+=strlen(pt);
if (*taille>SIZE_LINE) { printf("\n"); *taille=0; }
}

int main(int argc, char **argv)
{
char ch[TailleLigne],deca[TailleLigne],*pt;
int nb,taille,debsent,i,j;
lia_avl_t t_lexique;

if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s <list of non proper-name words>\n",argv[0]);
 exit(0);
 }

t_lexique=read_lexicon(argv[1]);
for(taille=0,debsent=0;fgets(ch,TailleLigne,stdin);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  {
  if (!strcmp(pt,"<s>")) { debsent=True; print_token(pt,&taille); }
  else
   if (!strcmp(pt,"</s>")) { printf(" %s\n",pt); taille=0; debsent=False; }
   else
    {
    if ((debsent)&&(IF_CAPITAL(pt[0]))&&(pt[1]=='\''))
     {
     if (taille>0) { printf(" "); taille++; }
     printf("%c' ",pt[0]+'a'-'A');
	 taille+=3;
     i=2;
     }
	else i=0;
     
    /* if the word WITH capital is in the lexicon => unchanged, and stop process (0106, DUC FRED) */
    if ((debsent)&&(IF_CAPITAL(pt[i]))&&(!lia_recherche_avl(t_lexique,pt+i,&nb)))
     {
     strcpy(deca,pt+i);
     decapital(deca);
     if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
     else
      if ((pt[i]=='E')&&!(IF_NUMBER(pt[i])))
       {
       sprintf(deca,"e1%s",pt+i+1); decapital(deca);
	   if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
	   else
        {
        sprintf(deca,"e2%s",pt+i+1); decapital(deca);
		if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
		else
         {
         sprintf(deca,"e2%s",pt+i+1); decapital(deca);
		 if (lia_recherche_avl(t_lexique,deca,&nb)) print_token(deca,&taille);
         else print_token(pt+i,&taille);
         }
        }
       }
      else
       if (inside_apos(deca,&j))
        {
        char sauv;
	sauv=deca[j+1];
        deca[j+1]='\0';
        if (lia_recherche_avl(t_lexique,deca,&nb))
	 {
	 print_token(deca,&taille);
	 if (sauv) { printf(" %c%s",sauv,deca+j+2); taille+=strlen(deca+j); }
	 } 
	else print_token(pt+i,&taille);
        }
       else print_token(pt+i,&taille);
     }
    else
     { debsent=False; print_token(pt+i,&taille); }
   if (!all_capital(pt+i)) debsent=False; /* if not ALL in capital, we stop the process after the first token (0106, DUC FRED) */
   }
  }

exit(0);
}
  
