/*  Take as input a biglex and list of pairs word pos and produce
 *  a new biglex with maybe some pruning and adjustment  */
/*  FRED 0309  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne     80000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/


#define IF_NUMBER(a)	(((a)>='0')&&((a)<='9'))

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

int if_standard(char *ch)
{
int i;
for(i=0;(ch[i])&&((ch[i]=='_')||(ch[i]=='-')||(ch[i]=='\'')||(if_lettre(ch[i]))||(IF_NUMBER(ch[i])));i++);
return ch[i]?False:True;
}

/*................................................................*/

char *T_cate[]={ "AMOUNT","FONC","LOC","ORG","PERS","PROD","TIME","" }; 

char *T_oldcate[]={ "XFAMIL","XPAYFP","XPAYFS","XPAYMP","XPAYMS","XPREF","XPREM","XSOC","XVILLE","" };
char *T_corresoldcate[]={ "XPERS","XLOC","XLOC","XLOC","XLOC","XPERS","XPERS","XORG","XLOC","" };

char *corres_old_cate(char *ch)
{
int i;
for(i=0;(T_oldcate[i][0])&&(strcmp(ch,T_oldcate[i]));i++);
if (T_oldcate[i][0]) return T_corresoldcate[i]; else return ch;
}

/*................................................................*/

/* Anglais NMS 625 Anglais NMP 1769 Anglais */

#define MAX_WORDS	2280000
#define MAX_FLEX	30

typedef struct
	{
	char *cate,*lemm;
	int nb;
	} type_flex;

typedef struct
	{
	char *word;
	type_flex t_flex[MAX_FLEX];
	} type_word;

type_word T_biglex[MAX_WORDS];

int load_biglex(FILE *filebiglex, int lexid, int nb)
{
char ch[TailleLigne],*chword,*chcate,*chnb,*chlemm;
int i,j,nboccu,cutoff,firstone,i_lex,nbline;

for(;fgets(ch,TailleLigne,filebiglex);)
 {
 chword=strtok(ch," \t\n"); if (!chword) ERREUR("bad format1:",ch);
 chcate=strtok(NULL," \t\n"); if (!chcate) ERREUR("bad format2:",ch);
 chcate=corres_old_cate(chcate);
 chnb=strtok(NULL," \t\n"); if (!chnb) ERREUR("bad format3:",ch);
 if (sscanf(chnb,"%d",&(nboccu))!=1) ERREUR("bad value:",chnb);
 chlemm=strtok(NULL," \t\n"); if (!chlemm) ERREUR("bad format4:",ch);
 if (!word2code(lexid,chword,&i_lex))
  {
  i_lex=nb++;
  add_word_lexicon(lexid,chword,i_lex);
  T_biglex[i_lex].word=strdup(chword);
  T_biglex[i_lex].t_flex[0].cate=NULL;
  }
 for(;chcate;)
  {
  if (strcmp(chcate,"MOTINC"))
   {
   for(j=0;(j<MAX_FLEX)&&(T_biglex[i_lex].t_flex[j].cate)&&(strcmp(chcate,T_biglex[i_lex].t_flex[j].cate));j++);
   if (j==MAX_FLEX) ERREUR("AJHGJHZFGGZF","");
   if (T_biglex[i_lex].t_flex[j].cate) T_biglex[i_lex].t_flex[j].nb+=nboccu;
   else
    {
    T_biglex[i_lex].t_flex[j].cate=strdup(chcate);
    T_biglex[i_lex].t_flex[j].lemm=strdup(chlemm);
    T_biglex[i_lex].t_flex[j].nb=nboccu;
    if (j+1==MAX_FLEX) ERREUR("AJHGJHZFGGZF2222","");
    T_biglex[i_lex].t_flex[j+1].cate=NULL;
    }
   }
  chcate=strtok(NULL," \t\n");
  if (chcate)
   {
   chcate=corres_old_cate(chcate);
   chnb=strtok(NULL," \t\n");
   if (!chnb) ERREUR("bad format5:",ch); chlemm=strtok(NULL," \t\n"); if (!chlemm) ERREUR("bad format6:",ch);
   if (sscanf(chnb,"%d",&(nboccu))!=1) ERREUR("bad value:",chnb);
   }
  }
 }
return nb;
}

int main(int argc, char **argv)
{
char ch[TailleLigne],*chword,*chcate,*chnb,*chlemm;
int nb,lexid,i,j,nboccu,cutoff,firstone,i_lex,nbline;
FILE *filebiglex,*filebiglex2;

cutoff=0;
filebiglex=filebiglex2=NULL;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-biglex"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   if (!(filebiglex=fopen(argv[++nb],"rt"))) ERREUR("can't open:",argv[nb]); 
   }
  else
  if (!strcmp(argv[nb],"-biglex2"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   if (!(filebiglex2=fopen(argv[++nb],"rt"))) ERREUR("can't open:",argv[nb]); 
   }
  else
  if (!strcmp(argv[nb],"-cutoff"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   if (sscanf(argv[++nb],"%d",&cutoff)!=1) ERREUR("bad value:",argv[nb]);
   }
  else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] -biglex <file> [-cutoff <int>]\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

if (!filebiglex) ERREUR("bad syntax, check '-h'","");
lexid=new_lexicon();
nb=load_biglex(filebiglex,lexid,0);
if (filebiglex2) nb=load_biglex(filebiglex2,lexid,nb);

for(nbline=0;fgets(ch,TailleLigne,stdin);nbline++)
 {
 /*
 chword=strtok(ch," \t\n"); if (!chword) ERREUR("bad format7:",ch);
 chcate=strtok(NULL," \t\n"); if (!chcate) { fprintf(stderr,"NB LINE = %d\n",nbline); ERREUR("bad format8:",ch); }
 */
 chword=strtok(ch," \t\n"); if (chword) chcate=strtok(NULL," \t\n"); else chcate=NULL;
 if ((chcate)&&(strcmp(chcate,"MOTINC"))&&(strcmp(chcate,"UNK")))
  { 
  chcate=corres_old_cate(chcate);
  if ((if_standard(chword))&&(if_standard(chcate)))
   {
   if (!word2code(lexid,chword,&i_lex))
    {
    i_lex=nb++;
    add_word_lexicon(lexid,chword,i_lex);
    T_biglex[i_lex].word=strdup(chword);
    T_biglex[i_lex].t_flex[0].cate=NULL;
    }
   for(j=0;(j<MAX_FLEX)&&(T_biglex[i_lex].t_flex[j].cate)&&(strcmp(chcate,T_biglex[i_lex].t_flex[j].cate));j++);
   if (j==MAX_FLEX) ERREUR("AJHGJHZFGGZF","");
   if (T_biglex[i_lex].t_flex[j].cate) T_biglex[i_lex].t_flex[j].nb++;
   else
    {
    T_biglex[i_lex].t_flex[j].cate=strdup(chcate);
    T_biglex[i_lex].t_flex[j].lemm=strdup(chword);
    T_biglex[i_lex].t_flex[j].nb=1;
    }
   }
  }
 }

for(i_lex=0;i_lex<nb;i_lex++)
 {
 for(firstone=True,j=0;(j<MAX_FLEX)&&(T_biglex[i_lex].t_flex[j].cate);j++) if (T_biglex[i_lex].t_flex[j].nb>cutoff)
  {
  if (firstone) { printf("%s",T_biglex[i_lex].word); firstone=False; }
  printf(" %s %d %s",T_biglex[i_lex].t_flex[j].cate,T_biglex[i_lex].t_flex[j].nb,T_biglex[i_lex].t_flex[j].lemm);
  }
 if (!firstone) printf("\n");
 }
fclose(filebiglex);

exit(0);
}
  
