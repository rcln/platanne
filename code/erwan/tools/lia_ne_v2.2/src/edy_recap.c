/*  Put back capital letter according to the NE tagger and a few rules  (for EDYLEX)  */
/*  FRED 0710  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne     8000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

void ERREURd(char *ch1, int i)
{
fprintf(stderr,"ERREUR : %s %d\n",ch1,i);
exit(0);
}

/*................................................................*/

/* possible tags:
<s>
</s>
<AMOUNT>
<FONC>
<LOC>
<ORG>
<PERS>
<PROD>
<TIME>
*/

#define T_NONE		0
#define T_AMOUNT	1
#define T_FONC		2
#define T_LOC		3
#define T_ORG		4
#define T_PERS		5
#define T_PROD		6
#define T_TIME		7

/*................................................................*/

#define IF_DIGIT(a) (((a)>='0')&&((a)<='9'))

/*................................................................*/

char upcasse(char c)
{
if ((c>='a')&&(c<='z')) return (c-'a')+'A';
 switch (c)
  {
    case 'é' : return 'É' ;
    case 'è' : return 'È' ;
    case 'ê' : return 'Ê' ;
    case 'ë' : return 'Ë' ;
    case 'à' : return 'À' ;
    case 'â' : return 'Â' ;
    case 'ä' : return 'Ä' ;
    case 'î' : return 'Î' ;
    case 'ï' : return 'Ï' ;
    case 'ô' : return 'Ô' ;
    case 'ö' : return 'Ö' ;
    case 'ù' : return 'Ù' ;
    case 'û' : return 'Û' ;
    case 'ü' : return 'Ü' ;
    case 'ç' : return 'Ç' ;
  }
return c;
}

/*................................................................*/

char *T_voie[]={"rue","avenue","boulevard","traverse","impasse","place","rond_point","carrefour","route","ville",""};

int in_liste(char *liste[], char *word)
{
int i;
for (i=0;(liste[i][0])&&(strcmp(liste[i],word));i++);
return liste[i][0];
}

/*................................................................*/

void last_chance_cap(int lexid, char *word)
{
static char ch[TailleLigne];
int i;
/* if the word is there, do nothing !! */
if (!word2code(lexid,word,NULL))
 {
 /* we try first the word with only 1 cap */
 strcpy(ch,word);
 ch[0]=upcasse(ch[0]);
 if (word2code(lexid,ch,NULL)) { strcpy(word,ch); }
 else
  {
  /* we try a cap after each '-' */
  for(i=0;ch[i];i++) if ((i>0)&&((ch[i-1]=='-')||(ch[i-1]=='\'')||(ch[i-1]=='_'))) ch[i]=upcasse(ch[i]);
  if (word2code(lexid,ch,NULL)) { strcpy(word,ch); }
  else
   { /* we try now with the word ALL in capital letter */
   for(i=0;word[i];i++) ch[i]=upcasse(word[i]);
   ch[i]='\0';
   if (word2code(lexid,ch,NULL)) strcpy(word,ch);
   }
  }
 }
}

void trycap(int lexid, char *word, int t_entity)
{
static char ch[TailleLigne];
int i;

/* we ignore if t_entity is T_LOC and word=rue,avenue,boulevard,traverse,.... */
if ((t_entity==T_LOC)&&(in_liste(T_voie,word))) { /* do nothing */ return; }

/* we try first the word with only 1 cap */
word[0]=upcasse(word[0]);
if (!word2code(lexid,word,NULL))
 {
 /* we try a cap after each '-' */
 for(i=0;word[i];i++) if ((i>0)&&((word[i-1]=='-')||(word[i-1]=='\'')||(word[i-1]=='_'))) word[i]=upcasse(word[i]);
 if (!word2code(lexid,word,NULL))
  { /* we try now with the word ALL in capital letter */
  for(i=0;word[i];i++) ch[i]=upcasse(word[i]);
  ch[i]='\0';
  if (word2code(lexid,ch,NULL)) strcpy(word,ch);
  }
 }
}

int main(int argc, char **argv)
{
int debsent,nb,putcap,prev,lexid,t_entity,i;
char ch[TailleLigne],*pt,prevword[TailleLigne];

lexid=-1;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-lex"))
   {
   if (nb+1==argc) ERREUR("must have a value after argument;",argv[nb]);
   lexid=load_lexicon_first_field(argv[++nb]);
   }
  else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] -lex <biglex_ne>\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

if (lexid==-1) ERREUR("bad syntax, check '-h'","");

putcap=prev=debsent=False;
while(fgets(ch,TailleLigne,stdin))
 {
 prevword[0]='\0';
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"))
  {
  if (!strcmp(pt,"<s>")) { debsent=True; } else
  if (!strcmp(pt,"</s>")) { putcap=False; printf("\n"); prev=False; } else
  if (!strcmp(pt,"<AMOUNT>")) { putcap=False; t_entity=T_AMOUNT; } else
  if (!strcmp(pt,"</AMOUNT>")) { putcap=False; t_entity=T_NONE; } else
  if (!strcmp(pt,"<FONC>")) { putcap=False; t_entity=T_FONC; } else
  if (!strcmp(pt,"</FONC>")) { putcap=False; t_entity=T_NONE; } else
  if (!strcmp(pt,"<LOC>")) { putcap=True; t_entity=T_LOC; } else
  if (!strcmp(pt,"</LOC>")) { putcap=False; t_entity=T_NONE; } else
  if (!strcmp(pt,"<ORG>")) { putcap=True; t_entity=T_ORG; } else
  if (!strcmp(pt,"</ORG>")) { putcap=False; t_entity=T_NONE; } else
  if (!strcmp(pt,"<PERS>")) { putcap=True; t_entity=T_PERS; } else
  if (!strcmp(pt,"</PERS>")) { putcap=False; t_entity=T_NONE; } else
  if (!strcmp(pt,"<PROD>")) { putcap=False; t_entity=T_PROD; } else
  if (!strcmp(pt,"</PROD>")) { putcap=False; t_entity=T_NONE; } else
  if (!strcmp(pt,"<TIME>")) { putcap=False; t_entity=T_TIME; } else
  if (!strcmp(pt,"</TIME>")) { putcap=False; t_entity=T_NONE; } else
   {
   if (debsent) pt[0]=upcasse(pt[0]);
   else
   if (putcap) trycap(lexid,pt,t_entity);
   else
   if (!strcmp(pt,"m.")) pt[0]=upcasse(pt[0]);
   debsent=False;

   if ((!strcmp(pt,"."))||(!strcmp(pt,","))) /* no space */;
   else
   if (((!strcmp(prevword,"."))||(!strcmp(prevword,",")))&&(IF_DIGIT(pt[0]))) /* no space */;
   else
   if (prevword[strlen(prevword)-1]=='\'') /* no space */;
   else
   if (prev) printf(" ");

   last_chance_cap(lexid,pt);
   for(i=0;pt[i];i++) printf("%c",pt[i]=='_'?' ':pt[i]);

   prev=True;
   strcpy(prevword,pt);
   }
  }
 }
exit(0);
}
  
